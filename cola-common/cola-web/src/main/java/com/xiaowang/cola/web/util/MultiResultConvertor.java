package com.xiaowang.cola.web.util;


import com.xiaowang.cola.base.response.PageResponse;
import com.xiaowang.cola.web.vo.MultiResult;

import static com.xiaowang.cola.base.response.ResponseCode.SUCCESS;

/**
 * @author cola
 */
public class MultiResultConvertor {

    public static <T> MultiResult<T> convert(PageResponse<T> pageResponse) {
        MultiResult<T> multiResult = new MultiResult<T>(true, SUCCESS.name(), SUCCESS.name(), pageResponse.getDatas(), pageResponse.getTotal(), pageResponse.getCurrentPage(), pageResponse.getPageSize());
        return multiResult;
    }
}
