package st.coo.memo.service;

import com.mybatisflex.core.audit.http.HttpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import st.coo.memo.common.BizException;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.sysConfig.SaveSysConfigRequest;
import st.coo.memo.dto.sysConfig.SysConfigDto;
import st.coo.memo.entity.TSysConfig;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.SysConfigMapperExt;
import st.coo.memo.mapper.UserMapperExt;
import st.coo.memo.util.JsonUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static st.coo.memo.entity.table.Tables.T_SYS_CONFIG;
import static st.coo.memo.entity.table.Tables.T_USER;

@Slf4j
@Component
public class SysConfigService {

    @Resource
    private SysConfigMapperExt sysConfigMapper;

    @Value("${official.square.url}")
    private String officialSquareUrl;

    @Resource
    private UserMapperExt userMapperExt;

    @Value("${MBLOG_EMBED:}")
    private String embed;

    @PostConstruct
    public void init() {
        TSysConfig config = sysConfigMapper.selectOneByQuery(QueryWrapper.create().and(T_SYS_CONFIG.KEY.eq(SysConfigConstant.WEB_HOOK_TOKEN)));
        String token = Optional.ofNullable(config).map(TSysConfig::getValue).orElse(null);
        if (StringUtils.hasText(token)) {
            return;
        }
        config = new TSysConfig();
        config.setKey(SysConfigConstant.WEB_HOOK_TOKEN);
        config.setValue(generateKey());
        sysConfigMapper.insertOrUpdate(config);
    }

    private String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            // 初始化密钥生成器，默认密钥长度
            keyGen.init(128);
            // 生成密钥
            SecretKey secretKey = keyGen.generateKey();
            // 获取二进制密钥
            byte[] binaryKey = secretKey.getEncoded();
            // 使用Base64编码将二进制密钥转换为String
            return Base64.getEncoder().encodeToString(binaryKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成密钥异常");
        }
    }

    public void save(SaveSysConfigRequest saveSysConfigRequest) {

        Optional<SysConfigDto> push2OfficialSquare = saveSysConfigRequest.getItems().stream()
                .filter(r -> Objects.equals(r.getKey(), SysConfigConstant.PUSH_OFFICIAL_SQUARE) && Objects.equals("true", r.getValue())).findFirst();


        if (push2OfficialSquare.isPresent()) {
            String token = getString(SysConfigConstant.WEB_HOOK_TOKEN);
            TUser admin = userMapperExt.selectOneByQuery(QueryWrapper.create().and(T_USER.ROLE.eq("ADMIN")));
            Optional<SysConfigDto> backendDomain = saveSysConfigRequest.getItems().stream()
                    .filter(r -> Objects.equals(r.getKey(), SysConfigConstant.DOMAIN) && Objects.equals("true", r.getValue())).findFirst();
            Optional<SysConfigDto> corsDomainList = saveSysConfigRequest.getItems().stream()
                    .filter(r -> Objects.equals(r.getKey(), SysConfigConstant.CORS_DOMAIN_LIST)).findFirst();
            String url = officialSquareUrl + "/api/token";
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("author", admin.getDisplayName());
            map.put("avatarUrl", admin.getAvatarUrl());
            if (StringUtils.hasText(embed) && backendDomain.isPresent()) {
                map.put("website", backendDomain.get().getValue());
            } else if (StringUtils.isEmpty(embed) && corsDomainList.isPresent() && StringUtils.hasText(corsDomainList.get().getValue())) {
                map.put("website", corsDomainList.get().getValue().split(",")[0]);
            }
            String body = JsonUtil.toJson(map);
            log.info("注册token {},body:{}", url, body);
            long curTime = System.currentTimeMillis();
            try {
                String response = HttpUtil.post(url, body, null);
                log.info("注册token,body:{},耗时:{}ms", response, System.currentTimeMillis() - curTime);
            } catch (Exception e) {
                log.error("注册token异常", e);
                throw new BizException(ResponseCode.fail, "连接广场异常,请查看后台日志");
            }
        }

        for (SysConfigDto item : saveSysConfigRequest.getItems()) {
            TSysConfig sysConfig = new TSysConfig();
            BeanUtils.copyProperties(item, sysConfig);
            sysConfigMapper.update(sysConfig);
        }
    }

    public List<SysConfigDto> getAll() {
        List<TSysConfig> list = sysConfigMapper.selectAll();
        return list.stream().map(r -> {
            SysConfigDto dto = new SysConfigDto();
            BeanUtils.copyProperties(r, dto);
            if (StringUtils.isEmpty(dto.getValue())) {
                dto.setValue(r.getDefaultValue());
            }
            return dto;
        }).toList();
    }

    public List<SysConfigDto> getAll(List<String> keys) {
        List<TSysConfig> list = sysConfigMapper.selectListByQuery(QueryWrapper.create().and(T_SYS_CONFIG.KEY.in(keys)));
        return list.stream().map(r -> {
            SysConfigDto dto = new SysConfigDto();
            BeanUtils.copyProperties(r, dto);
            if (StringUtils.isEmpty(dto.getValue())) {
                dto.setValue(r.getDefaultValue());
            }
            return dto;
        }).toList();
    }

    public boolean getBoolean(String key) {
        String value = getString(key);
        if (value == null) return false;
        return Boolean.valueOf(value);
    }

    public long getNumber(String key) {
        String value = getString(key);
        if (value == null) return 0;
        return Long.parseLong(value);
    }

    public String getString(String key) {
        TSysConfig sysConfig = sysConfigMapper.selectOneById(key);
        if (sysConfig == null) {
            return null;
        }
        return sysConfig.getValue() != null ? sysConfig.getValue() : sysConfig.getDefaultValue();
    }
}
