package org.dddjava.jig.domain.model.information.outbound.mybatis.ut;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TraceMapper {
    boolean binding(String key);
}
