package st.coo.memo.common;

import cn.dev33.satoken.stp.StpInterface;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.UserMapperExt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserMapperExt userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        int id = Integer.parseInt(loginId.toString());
        TUser user = userMapper.selectOneById(id);
        if (user != null) {
            return List.of(user.getRole());
        }
        return Collections.emptyList();
    }
}
