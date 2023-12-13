package st.coo.memo.dto.sysConfig;

import lombok.Data;

import java.util.List;

@Data

public class SaveSysConfigRequest {
    private List<SysConfigDto> items;
}
