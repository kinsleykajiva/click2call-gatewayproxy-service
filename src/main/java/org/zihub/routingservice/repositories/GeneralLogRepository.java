package org.zihub.routingservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zihub.routingservice.dbaccess.Domain;
import org.zihub.routingservice.dbaccess.GeneralLog;

public interface GeneralLogRepository  extends JpaRepository<GeneralLog, Integer> {



}
