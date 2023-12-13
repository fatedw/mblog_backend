package st.coo.memo.common.jwt;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.exception.ApiDisabledException;
import cn.dev33.satoken.listener.SaTokenEventCenter;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @Author sivan.wxw
 * @Date 2023/12/13 10:43
 */
public class StpLogicJwtForStateless extends StpLogic {

    /**
     * key：账号类型
     */
    public static final String LOGIN_TYPE = "loginType";

    /**
     * key：账号id
     */
    public static final String LOGIN_ID = "loginId";

    /**
     * key：登录设备类型
     */
    public static final String DEVICE = "device";

    /**
     * key：有效截止期 (时间戳)
     */
    public static final String EFF = "eff";

    /**
     * key：乱数 （ 混入随机字符串，防止每次生成的 token 都是一样的 ）
     */
    public static final String RN_STR = "rnStr";

    /**
     * Sa-Token 整合 jwt -- Stateless 无状态
     */
    public StpLogicJwtForStateless() {
        super(StpUtil.TYPE);
    }

    /**
     * Sa-Token 整合 jwt -- Stateless 无状态
     *
     * @param loginType 账号体系标识
     */
    public StpLogicJwtForStateless(String loginType) {
        super(loginType);
    }

    /**
     * 获取jwt秘钥
     *
     * @return /
     */
    public String jwtSecretKey() {
        String keyt = getConfigOrGlobal().getJwtSecretKey();
        SaJwtException.throwByNull(keyt, "请配置jwt秘钥", SaJwtErrorCode.CODE_30205);
        return keyt;
    }

    //
    // ------ 重写方法
    //

    // ------------------- 获取token 相关 -------------------

    /**
     * 创建一个TokenValue
     */
    @Override
    public String createTokenValue(Object loginId, String device, long timeout, Map<String, Object> extraData) {
        long effTime = timeout;
        if (timeout != SaTokenDao.NEVER_EXPIRE) {
            effTime = timeout * 1000 + System.currentTimeMillis();
        }
        Map<String, Object> map = new HashMap<>();
        map.put(LOGIN_ID, loginId);
        if (extraData!= null) {
            map.putAll(extraData);
        }
        // 创建
        return JWT.create()
                .withClaim(LOGIN_TYPE, loginType)
                .withClaim(DEVICE, device)
                .withClaim(EFF, effTime)
                // 塞入一个随机字符串，防止同账号同一毫秒下每次生成的 token 都一样的
                .withClaim(RN_STR, SaFoxUtil.getRandomString(32))
                .withPayload(map)
                .sign(Algorithm.HMAC256(jwtSecretKey()));
    }

    /**
     * 获取当前会话的Token信息
     *
     * @return token信息
     */
    @Override
    public SaTokenInfo getTokenInfo() {
        SaTokenInfo info = new SaTokenInfo();
        info.tokenName = getTokenName();
        info.tokenValue = getTokenValue();
        info.isLogin = isLogin();
        info.loginId = getLoginIdDefaultNull();
        info.loginType = getLoginType();
        info.tokenTimeout = getTokenTimeout();
        info.sessionTimeout = SaTokenDao.NOT_VALUE_EXPIRE;
        info.tokenSessionTimeout = SaTokenDao.NOT_VALUE_EXPIRE;
        info.tokenActiveTimeout = SaTokenDao.NOT_VALUE_EXPIRE;
        info.loginDevice = getLoginDevice();
        return info;
    }

    // ------------------- 登录相关操作 -------------------

    /**
     * 创建指定账号id的登录会话
     *
     * @param id         登录id，建议的类型：（long | int | String）
     * @param loginModel 此次登录的参数Model
     * @return 返回会话令牌
     */
    @Override
    public String createLoginSession(Object id, SaLoginModel loginModel) {

        // 1、先检查一下，传入的参数是否有效
        checkLoginArgs(id, loginModel);

        // 2、初始化 loginModel ，给一些参数补上默认值
        loginModel.build(getConfigOrGlobal());

        // 3、生成一个token
        String tokenValue = createTokenValue(id, loginModel.getDeviceOrDefault(), loginModel.getTimeout(), loginModel.getExtraData());

        // 4、$$ 发布事件：账号xxx 登录成功
        SaTokenEventCenter.doLogin(loginType, id, tokenValue, loginModel);

        // 5、返回
        return tokenValue;
    }

    /**
     * 获取指定Token对应的账号id (不做任何特殊处理)
     */
    @Override
    public String getLoginIdNotHandle(String tokenValue) {
        try {
            Map<String, Claim> map = checkAndGetClaims(tokenValue, loginType, true);
            return Optional.ofNullable(map).map(m -> m.get(LOGIN_ID)).map(Claim::asInt).map(String::valueOf).orElse(null);
        } catch (SaJwtException e) {
            return null;
        }
    }

    private Map<String, Claim> checkAndGetClaims(String token, String loginType, boolean isCheckTimeout) {
        String keyt = jwtSecretKey();
        // 秘钥不可以为空
        if (SaFoxUtil.isEmpty(keyt)) {
            throw new SaJwtException("请配置 jwt 秘钥");
        }
        // 如果token为null
        if (token == null) {
            throw new SaJwtException("jwt 字符串不可为空");
        }

        // 解析
        DecodedJWT jwt;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecretKey()))
                    .build(); // 可重用的验证器实例
            jwt = verifier.verify(token);
        } catch (JWTVerificationException exception) {
            // 验证失败，处理异常...
            throw new SaJwtException("jwt 签名无效：" + token).setCode(SaJwtErrorCode.CODE_30202);
        }
        // 校验 loginType
        String jwtLoginType = jwt.getClaim(LOGIN_TYPE).asString();
        if (!Objects.equals(loginType, jwtLoginType)) {
            throw new SaJwtException("jwt loginType 无效：" + token).setCode(SaJwtErrorCode.CODE_30203);
        }

        // 校验 Token 有效期
        if (isCheckTimeout) {
            Long effTime = jwt.getClaim(EFF).asLong();
            if (effTime != SaTokenDao.NEVER_EXPIRE) {
                if (effTime == null || effTime < System.currentTimeMillis()) {
                    throw new SaJwtException("jwt 已过期：" + token).setCode(SaJwtErrorCode.CODE_30204);
                }
            }
        }

        // 返回
        return jwt.getClaims();
    }


    /**
     * 会话注销
     */
    @Override
    public void logout() {
        // 如果连token都没有，那么无需执行任何操作
        String tokenValue = getTokenValue();
        if (SaFoxUtil.isEmpty(tokenValue)) {
            return;
        }

        // 从当前 [storage存储器] 里删除
        SaHolder.getStorage().delete(splicingKeyJustCreatedSave());

        // 如果打开了Cookie模式，则把cookie清除掉
        if (getConfigOrGlobal().getIsReadCookie()) {
            SaHolder.getResponse().deleteCookie(getTokenName());
        }
    }

    /**
     * 获取当前 Token 的扩展信息
     */
    @Override
    public Object getExtra(String key) {
        return getExtra(getTokenValue(), key);
    }

    /**
     * 获取指定 Token 的扩展信息
     */
    @Override
    public Object getExtra(String tokenValue, String key) {
        Map<String, Claim> map = checkAndGetClaims(tokenValue, loginType, true);
        return Optional.ofNullable(map).map(m -> m.get(LOGIN_ID)).map(Claim::asString).orElse(null);
    }


    // ------------------- 过期时间相关 -------------------

    /**
     * 获取当前登录者的 token 剩余有效时间 (单位: 秒)
     */
    @Override
    public long getTokenTimeout() {
        try {
            Map<String, Claim> payloads = checkAndGetClaims(getTokenValue(), loginType, true);
            // 如果被设置为：永不过期
            Long effTime = Optional.ofNullable(payloads).map(m -> m.get(EFF)).map(Claim::asLong).orElse(null);
            if (effTime == SaTokenDao.NEVER_EXPIRE) {
                return SaTokenDao.NEVER_EXPIRE;
            }
            // 如果已经超时
            if (effTime == null || effTime < System.currentTimeMillis()) {
                return SaTokenDao.NOT_VALUE_EXPIRE;
            }

            // 计算timeout (转化为以秒为单位的有效时间)
            return (effTime - System.currentTimeMillis()) / 1000;
        } catch (Exception e) {
            return SaTokenDao.NOT_VALUE_EXPIRE;
        }
    }


    // ------------------- id 反查 token 相关操作 -------------------

    /**
     * 返回当前会话的登录设备类型
     *
     * @return 当前令牌的登录设备类型
     */
    @Override
    public String getLoginDevice() {
        // 如果没有token，直接返回 null
        String tokenValue = getTokenValue();
        if (tokenValue == null) {
            return null;
        }
        // 如果还未登录，直接返回 null
        if (!isLogin()) {
            return null;
        }
        // 获取
        Map<String, Claim> map = checkAndGetClaims(tokenValue, loginType, false);
        return Optional.ofNullable(map).map(m -> m.get(DEVICE)).map(Claim::asString).orElse(null);
    }


    // ------------------- Bean对象代理 -------------------

    /**
     * [禁用] 返回持久化对象
     */
    @Override
    public SaTokenDao getSaTokenDao() {
        throw new ApiDisabledException();
    }

    /**
     * 重写返回：支持 extra 扩展参数
     */
    @Override
    public boolean isSupportExtra() {
        return true;
    }

}
