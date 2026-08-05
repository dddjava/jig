package org.dddjava.jig.domain.model.information.outbound.mybatis.sut;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TraceMapper {
    boolean binding(String key);
}
