package com.aws.sync.vo;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * <p>
 * 用户登录，参数传递
 * </p>
 *
 * @author Wang Chen Chen <932560435@qq.com>
 * @version 2.0
 * @date 2019/4/18 11:45
 */


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserVO implements Serializable {


    @NotNull
    private String msg;

    private String token;


}
