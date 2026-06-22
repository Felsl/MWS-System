package org.lvtn.mws.domain.repository;

/** Port sinh so_number hiển thị (vd SO-yyyyMMdd-####). */
public interface ISalesOrderNumberGenerator {
    String next();
}
