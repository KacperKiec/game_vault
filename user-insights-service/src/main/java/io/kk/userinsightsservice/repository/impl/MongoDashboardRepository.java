package io.kk.userinsightsservice.repository.impl;

import io.kk.userinsightsservice.model.mongo.DashboardDocument;
import io.kk.userinsightsservice.repository.DashboardRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoDashboardRepository extends MongoRepository<DashboardDocument, String>, DashboardRepository {

}
