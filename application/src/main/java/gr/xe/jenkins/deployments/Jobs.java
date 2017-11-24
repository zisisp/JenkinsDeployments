package gr.xe.jenkins.deployments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 20/11/2017
 * Time: 1:46 μμ
 * Company: www.xe.gr
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jobs {
    private String _class;
    private String name;
    private String url;
    private String color;
}