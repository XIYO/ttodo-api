package point.ttodoApi.shared.validation.sanitizer;

import org.owasp.html.*;
import org.springframework.context.annotation.*;

@Configuration
public class HtmlSanitizerConfig {

    @Bean
    public PolicyFactory htmlSanitizerPolicy() {
        return new HtmlPolicyBuilder()
                .allowElements("p", "div", "span", "br", "hr")
                .allowElements("b", "i", "u", "strong", "em", "mark", "small", "sub", "sup")
                .allowElements("h1", "h2", "h3", "h4", "h5", "h6")
                .allowElements("ul", "ol", "li")
                .allowElements("a")
                .allowAttributes("href").onElements("a")
                .requireRelNofollowOnLinks()
                .allowElements("blockquote", "q", "cite")
                .allowElements("code", "pre")
                .allowElements("table", "thead", "tbody", "tfoot", "tr", "th", "td")
                .allowAttributes("colspan", "rowspan").onElements("th", "td")
                .disallowElements("script", "style", "iframe", "object", "embed", "form")
                .allowAttributes("class", "id").globally()
                .toFactory();
    }

    @Bean
    public PolicyFactory strictHtmlSanitizerPolicy() {
        return new HtmlPolicyBuilder()
                .allowElements("p", "br")
                .allowElements("b", "i", "u", "strong", "em")
                .toFactory();
    }

    @Bean
    public PolicyFactory noHtmlPolicy() {
        return new HtmlPolicyBuilder()
                .toFactory();
    }
}