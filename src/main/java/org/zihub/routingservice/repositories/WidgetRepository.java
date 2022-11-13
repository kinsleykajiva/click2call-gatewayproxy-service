package org.zihub.routingservice.repositories;

import org.zihub.routingservice.dbaccess.Widget;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WidgetRepository extends JpaRepository<Widget, Integer> {


    Widget findByCompanyId(int companyId);
    Widget findById(int id);
}