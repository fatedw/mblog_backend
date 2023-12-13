package st.coo.memo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rss")
public class RssController {


    @GetMapping
    public String rss(HttpServletResponse httpServletResponse){
        return "not support";
    }
}
