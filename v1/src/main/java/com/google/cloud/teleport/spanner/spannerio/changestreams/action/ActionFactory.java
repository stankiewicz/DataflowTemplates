/*
 * Copyright (C) 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.spanner.spannerio.changestreams.action;

import com.google.cloud.teleport.spanner.spannerio.changestreams.ChangeStreamMetrics;
import com.google.cloud.teleport.spanner.spannerio.changestreams.cache.WatermarkCache;
import com.google.cloud.teleport.spanner.spannerio.changestreams.dao.ChangeStreamDao;
import com.google.cloud.teleport.spanner.spannerio.changestreams.dao.PartitionMetadataDao;
import com.google.cloud.teleport.spanner.spannerio.changestreams.estimator.ThroughputEstimator;
import com.google.cloud.teleport.spanner.spannerio.changestreams.mapper.ChangeStreamRecordMapper;
import com.google.cloud.teleport.spanner.spannerio.changestreams.mapper.PartitionMetadataMapper;
import com.google.cloud.teleport.spanner.spannerio.changestreams.model.DataChangeRecord;
import java.io.Serializable;
import org.joda.time.Duration;

/**
 * Factory class for creating instances that will handle each type of record within a change stream
 * query. The instances created are all singletons.
 */
// transient fields are un-initialized, because we start them during the first fetch call (with the
// singleton pattern).
@SuppressWarnings("initialization.field.uninitialized")
public class ActionFactory implements Serializable {

  private static final long serialVersionUID = -4060958761369602619L;
  private transient DataChangeRecordAction dataChangeRecordActionInstance;
  private transient com.google.cloud.teleport.spanner.spannerio.changestreams.action
          .HeartbeatRecordAction
      heartbeatRecordActionInstance;
  private transient com.google.cloud.teleport.spanner.spannerio.changestreams.action
          .ChildPartitionsRecordAction
      childPartitionsRecordActionInstance;
  private transient QueryChangeStreamAction queryChangeStreamActionInstance;
  private transient DetectNewPartitionsAction detectNewPartitionsActionInstance;

  /**
   * Creates and returns a singleton instance of an action class capable of processing {@link
   * DataChangeRecord}s.
   *
   * <p>This method is thread safe.
   *
   * @return singleton instance of the {@link DataChangeRecordAction}
   */
  public synchronized DataChangeRecordAction dataChangeRecordAction(
      ThroughputEstimator<DataChangeRecord> throughputEstimator) {
    if (dataChangeRecordActionInstance == null) {
      dataChangeRecordActionInstance = new DataChangeRecordAction(throughputEstimator);
    }
    return dataChangeRecordActionInstance;
  }

  /**
   * Creates and returns a singleton instance of an action class capable of processing {@link
   * com.google.cloud.teleport.spanner.spannerio.changestreams.model.HeartbeatRecord}s. This method
   * is thread safe.
   *
   * @param metrics metrics gathering class
   * @return singleton instance of the {@link
   *     com.google.cloud.teleport.spanner.spannerio.changestreams.action.HeartbeatRecordAction}
   */
  public synchronized com.google.cloud.teleport.spanner.spannerio.changestreams.action
          .HeartbeatRecordAction
      heartbeatRecordAction(ChangeStreamMetrics metrics) {
    if (heartbeatRecordActionInstance == null) {
      heartbeatRecordActionInstance =
          new com.google.cloud.teleport.spanner.spannerio.changestreams.action
              .HeartbeatRecordAction(metrics);
    }
    return heartbeatRecordActionInstance;
  }

  /**
   * Creates and returns a singleton instance of an action class capable of process {@link
   * com.google.cloud.teleport.spanner.spannerio.changestreams.model.ChildPartitionsRecord}s. This
   * method is thread safe.
   *
   * @param partitionMetadataDao DAO class to access the Connector's metadata tables
   * @param metrics metrics gathering class
   * @return singleton instance of the {@link
   *     com.google.cloud.teleport.spanner.spannerio.changestreams.action.ChildPartitionsRecordAction}
   */
  public synchronized com.google.cloud.teleport.spanner.spannerio.changestreams.action
          .ChildPartitionsRecordAction
      childPartitionsRecordAction(
          PartitionMetadataDao partitionMetadataDao, ChangeStreamMetrics metrics) {
    if (childPartitionsRecordActionInstance == null) {
      childPartitionsRecordActionInstance =
          new com.google.cloud.teleport.spanner.spannerio.changestreams.action
              .ChildPartitionsRecordAction(partitionMetadataDao, metrics);
    }
    return childPartitionsRecordActionInstance;
  }

  /**
   * Creates and returns a single instance of an action class capable of performing a change stream
   * query for a given partition. It uses the {@link DataChangeRecordAction}, {@link
   * com.google.cloud.teleport.spanner.spannerio.changestreams.action.HeartbeatRecordAction} and
   * {@link
   * com.google.cloud.teleport.spanner.spannerio.changestreams.action.ChildPartitionsRecordAction}
   * to dispatch the necessary processing depending on the type of record received.
   *
   * @param changeStreamDao DAO class to perform a change stream query
   * @param partitionMetadataDao DAO class to access the Connector's metadata tables
   * @param changeStreamRecordMapper mapper class to transform change stream records into the
   *     Connector's domain models
   * @param partitionMetadataMapper mapper class to transform partition metadata rows into the
   *     Connector's domain models
   * @param dataChangeRecordAction action class to process {@link DataChangeRecord}s
   * @param heartbeatRecordAction action class to process {@link
   *     com.google.cloud.teleport.spanner.spannerio.changestreams.model.HeartbeatRecord}s
   * @param childPartitionsRecordAction action class to process {@link
   *     com.google.cloud.teleport.spanner.spannerio.changestreams.model.ChildPartitionsRecord}s
   * @param metrics metrics gathering class
   * @return single instance of the {@link QueryChangeStreamAction}
   */
  public synchronized QueryChangeStreamAction queryChangeStreamAction(
      ChangeStreamDao changeStreamDao,
      PartitionMetadataDao partitionMetadataDao,
      ChangeStreamRecordMapper changeStreamRecordMapper,
      PartitionMetadataMapper partitionMetadataMapper,
      DataChangeRecordAction dataChangeRecordAction,
      HeartbeatRecordAction heartbeatRecordAction,
      ChildPartitionsRecordAction childPartitionsRecordAction,
      ChangeStreamMetrics metrics) {
    if (queryChangeStreamActionInstance == null) {
      queryChangeStreamActionInstance =
          new QueryChangeStreamAction(
              changeStreamDao,
              partitionMetadataDao,
              changeStreamRecordMapper,
              partitionMetadataMapper,
              dataChangeRecordAction,
              heartbeatRecordAction,
              childPartitionsRecordAction,
              metrics);
    }
    return queryChangeStreamActionInstance;
  }

  /**
   * Creates and returns a single instance of an action class capable of detecting and scheduling
   * new partitions to be queried.
   *
   * @param partitionMetadataDao DAO class to access the Connector's metadata tables
   * @param partitionMetadataMapper mapper class to transform partition metadata table rows into the
   *     Connector's domain models
   * @param metrics metrics gathering class
   * @param resumeDuration specifies the periodic schedule to re-execute the action
   * @return single instance of the {@link DetectNewPartitionsAction}
   */
  public synchronized DetectNewPartitionsAction detectNewPartitionsAction(
      PartitionMetadataDao partitionMetadataDao,
      PartitionMetadataMapper partitionMetadataMapper,
      WatermarkCache watermarkCache,
      ChangeStreamMetrics metrics,
      Duration resumeDuration) {
    if (detectNewPartitionsActionInstance == null) {
      detectNewPartitionsActionInstance =
          new DetectNewPartitionsAction(
              partitionMetadataDao,
              partitionMetadataMapper,
              watermarkCache,
              metrics,
              resumeDuration);
    }
    return detectNewPartitionsActionInstance;
  }
}
