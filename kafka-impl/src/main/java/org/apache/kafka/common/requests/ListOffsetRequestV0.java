/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.common.requests;

import static org.apache.kafka.common.protocol.types.Type.INT32;
import static org.apache.kafka.common.protocol.types.Type.INT64;
import static org.apache.kafka.common.protocol.types.Type.INT8;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.message.ListOffsetsRequestData;
import org.apache.kafka.common.message.ListOffsetsResponseData;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.protocol.types.ArrayOf;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Struct;

public class ListOffsetRequestV0 extends AbstractRequest {

    public static final Field.Str TOPIC_NAME = new Field.Str("topic", "Name of topic");
    public static final Field.Int32 PARTITION_ID = new Field.Int32("partition", "Topic partition id");

    public static final long EARLIEST_TIMESTAMP = -2L;
    public static final long LATEST_TIMESTAMP = -1L;

    public static final int CONSUMER_REPLICA_ID = -1;
    public static final int DEBUGGING_REPLICA_ID = -2;

    private static final String REPLICA_ID_KEY_NAME = "replica_id";
    private static final String ISOLATION_LEVEL_KEY_NAME = "isolation_level";
    private static final String TOPICS_KEY_NAME = "topics";

    // topic level field names
    private static final String PARTITIONS_KEY_NAME = "partitions";

    // partition level field names
    private static final String TIMESTAMP_KEY_NAME = "timestamp";
    private static final String MAX_NUM_OFFSETS_KEY_NAME = "max_num_offsets";

    private static final Schema LIST_OFFSET_REQUEST_PARTITION_V0 = new Schema(
            PARTITION_ID,
            new Field(TIMESTAMP_KEY_NAME, INT64, "Timestamp."),
            new Field(MAX_NUM_OFFSETS_KEY_NAME, INT32, "Maximum offsets to return."));
    private static final Schema LIST_OFFSET_REQUEST_PARTITION_V1 = new Schema(
            PARTITION_ID,
            new Field(TIMESTAMP_KEY_NAME, INT64, "The target timestamp for the partition."));

    private static final Schema LIST_OFFSET_REQUEST_TOPIC_V0 = new Schema(
            TOPIC_NAME,
            new Field(PARTITIONS_KEY_NAME, new ArrayOf(LIST_OFFSET_REQUEST_PARTITION_V0),
                    "Partitions to list offset."));
    private static final Schema LIST_OFFSET_REQUEST_TOPIC_V1 = new Schema(
            TOPIC_NAME,
            new Field(PARTITIONS_KEY_NAME, new ArrayOf(LIST_OFFSET_REQUEST_PARTITION_V1),
                    "Partitions to list offset."));

    private static final Schema LIST_OFFSET_REQUEST_V0 = new Schema(
            new Field(REPLICA_ID_KEY_NAME, INT32, "Broker id of the follower. For normal consumers, use -1."),
            new Field(TOPICS_KEY_NAME, new ArrayOf(LIST_OFFSET_REQUEST_TOPIC_V0), "Topics to list offsets."));
    private static final Schema LIST_OFFSET_REQUEST_V1 = new Schema(
            new Field(REPLICA_ID_KEY_NAME, INT32, "Broker id of the follower. For normal consumers, use -1."),
            new Field(TOPICS_KEY_NAME, new ArrayOf(LIST_OFFSET_REQUEST_TOPIC_V1), "Topics to list offsets."));

    private static final Schema LIST_OFFSET_REQUEST_V2 = new Schema(
            new Field(REPLICA_ID_KEY_NAME, INT32, "Broker id of the follower. For normal consumers, use -1."),
            new Field(ISOLATION_LEVEL_KEY_NAME, INT8,
                    "This setting controls the visibility of transactional records. "
                    + "Using READ_UNCOMMITTED (isolation_level = 0) makes all records visible. With READ_COMMITTED "
                    + "(isolation_level = 1), non-transactional and COMMITTED transactional records are visible. "
                    + "To be more concrete, READ_COMMITTED returns all data from offsets smaller than the current "
                    + "LSO (last stable offset), and enables the inclusion of the list of aborted transactions in the "
                    + "result, which allows consumers to discard ABORTED transactional records"),
            new Field(TOPICS_KEY_NAME, new ArrayOf(LIST_OFFSET_REQUEST_TOPIC_V1), "Topics to list offsets."));

    /**
     * The version number is bumped to indicate that on quota violation brokers send out responses before throttling.
     */
    private static final Schema LIST_OFFSET_REQUEST_V3 = LIST_OFFSET_REQUEST_V2;

    private final int replicaId;
    private final IsolationLevel isolationLevel;
    private final Map<TopicPartition, ListOffsetRequestV0.PartitionData> offsetData;
    private final Map<TopicPartition, Long> partitionTimestamps;
    private final Set<TopicPartition> duplicatePartitions;

    private ListOffsetsRequestData data;

    @Override
    public ApiMessage data() {
        return data;
    }

    public static class Builder extends AbstractRequest.Builder<ListOffsetRequestV0> {
        private final int replicaId;
        private final IsolationLevel isolationLevel;
        private Map<TopicPartition, ListOffsetRequestV0.PartitionData> offsetData = null;
        private Map<TopicPartition, Long> partitionTimestamps = null;

        public static ListOffsetRequestV0.Builder forReplica(short allowedVersion, int replicaId) {
            return new ListOffsetRequestV0.Builder((short) 0, allowedVersion, replicaId,
                    IsolationLevel.READ_UNCOMMITTED);
        }

        public static ListOffsetRequestV0.Builder forConsumer(boolean requireTimestamp, IsolationLevel isolationLevel) {
            short minVersion = 0;
            if (isolationLevel == IsolationLevel.READ_COMMITTED) {
                minVersion = 2;
            } else if (requireTimestamp){
                minVersion = 1;
            }
            return new ListOffsetRequestV0.Builder(minVersion,
                    ApiKeys.LIST_OFFSETS.latestVersion(),
                    CONSUMER_REPLICA_ID, isolationLevel);
        }

        private Builder(short oldestAllowedVersion, short latestAllowedVersion, int replicaId,
                        IsolationLevel isolationLevel) {
            super(ApiKeys.LIST_OFFSETS, oldestAllowedVersion, latestAllowedVersion);
            this.replicaId = replicaId;
            this.isolationLevel = isolationLevel;
        }

        public ListOffsetRequestV0.Builder setOffsetData(Map<TopicPartition,
                ListOffsetRequestV0.PartitionData> offsetData) {
            this.offsetData = offsetData;
            return this;
        }

        public ListOffsetRequestV0.Builder setTargetTimes(Map<TopicPartition, Long> partitionTimestamps) {
            this.partitionTimestamps = partitionTimestamps;
            return this;
        }

        @Override
        public ListOffsetRequestV0 build(short version) {
            if (version == 0) {
                if (offsetData == null) {
                    if (partitionTimestamps == null) {
                        throw new IllegalArgumentException(
                                "Must set partitionTimestamps or offsetData when creating a v0 ListOffsetRequest");
                    } else {
                        offsetData = new HashMap<>();
                        for (Map.Entry<TopicPartition, Long> entry: partitionTimestamps.entrySet()) {
                            offsetData.put(entry.getKey(),
                                    new ListOffsetRequestV0.PartitionData(entry.getValue(), 1));
                        }
                        this.partitionTimestamps = null;
                    }
                }
            } else {
                if (offsetData != null) {
                    throw new IllegalArgumentException("Cannot create a v" + version + " ListOffsetRequest with v0 "
                            + "PartitionData.");
                } else if (partitionTimestamps == null) {
                    throw new IllegalArgumentException("Must set partitionTimestamps when creating a v"
                            + version + " ListOffsetRequest");
                }
            }
            Map<TopicPartition, ?> m = (version == 0) ?  offsetData : partitionTimestamps;
            return new ListOffsetRequestV0(replicaId, m, isolationLevel, version, null);
        }

        @Override
        public String toString() {
            StringBuilder bld = new StringBuilder();
            bld.append("(type=ListOffsetRequest")
                    .append(", replicaId=").append(replicaId);
            if (offsetData != null) {
                bld.append(", offsetData=").append(offsetData);
            }
            if (partitionTimestamps != null) {
                bld.append(", partitionTimestamps=").append(partitionTimestamps);
            }
            bld.append(", isolationLevel=").append(isolationLevel);
            bld.append(")");
            return bld.toString();
        }
    }

    /**
     * This class is only used by ListOffsetRequest v0 which has been deprecated.
     */
    @Deprecated
    public static final class PartitionData {
        public final long timestamp;
        public final int maxNumOffsets;

        public PartitionData(long timestamp, int maxNumOffsets) {
            this.timestamp = timestamp;
            this.maxNumOffsets = maxNumOffsets;
        }

        @Override
        public String toString() {
            StringBuilder bld = new StringBuilder();
            bld.append("{timestamp: ").append(timestamp).
                    append(", maxNumOffsets: ").append(maxNumOffsets).
                    append("}");
            return bld.toString();
        }
    }

    /**
     * Private constructor with a specified version.
     */
    @SuppressWarnings("unchecked")
    private ListOffsetRequestV0(int replicaId, Map<TopicPartition, ?> targetTimes,
                                IsolationLevel isolationLevel, short version, ListOffsetsRequestData data) {
        super(ApiKeys.LIST_OFFSETS, version);
        this.replicaId = replicaId;
        this.isolationLevel = isolationLevel;
        this.offsetData = version == 0 ? (Map<TopicPartition, ListOffsetRequestV0.PartitionData>) targetTimes : null;
        this.partitionTimestamps = version >= 1 ? (Map<TopicPartition, Long>) targetTimes : null;
        this.duplicatePartitions = Collections.emptySet();
        this.data = data;
    }

    public ListOffsetRequestV0(Struct struct, short version) {
        super(ApiKeys.LIST_OFFSETS, version);
        Set<TopicPartition> duplicatePartitions = new HashSet<>();
        replicaId = struct.getInt(REPLICA_ID_KEY_NAME);
        isolationLevel = struct.hasField(ISOLATION_LEVEL_KEY_NAME)
                ? IsolationLevel.forId(struct.getByte(ISOLATION_LEVEL_KEY_NAME)) : IsolationLevel.READ_UNCOMMITTED;
        offsetData = new HashMap<>();
        partitionTimestamps = new HashMap<>();
        for (Object topicResponseObj : struct.getArray(TOPICS_KEY_NAME)) {
            Struct topicResponse = (Struct) topicResponseObj;
            String topic = topicResponse.get(TOPIC_NAME);
            for (Object partitionResponseObj : topicResponse.getArray(PARTITIONS_KEY_NAME)) {
                Struct partitionResponse = (Struct) partitionResponseObj;
                int partition = partitionResponse.get(PARTITION_ID);
                long timestamp = partitionResponse.getLong(TIMESTAMP_KEY_NAME);
                TopicPartition tp = new TopicPartition(topic, partition);
                if (partitionResponse.hasField(MAX_NUM_OFFSETS_KEY_NAME)) {
                    int maxNumOffsets = partitionResponse.getInt(MAX_NUM_OFFSETS_KEY_NAME);
                    ListOffsetRequestV0.PartitionData partitionData =
                            new ListOffsetRequestV0.PartitionData(timestamp, maxNumOffsets);
                    offsetData.put(tp, partitionData);
                } else {
                    if (partitionTimestamps.put(tp, timestamp) != null){
                        duplicatePartitions.add(tp);
                    }
                }
            }
        }
        this.duplicatePartitions = duplicatePartitions;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AbstractResponse getErrorResponse(int throttleTimeMs, Throwable e) {
        ListOffsetsResponseData responseData = new ListOffsetsResponseData();
        responseData.setThrottleTimeMs(throttleTimeMs);
        Errors errors = Errors.forException(e);

        List<String> topics = offsetData.keySet()
                .stream()
                .map(TopicPartition::topic)
                .distinct()
                .collect(Collectors.toList());

        short versionId = version();

        topics.forEach(topic -> {
            ListOffsetsResponseData.ListOffsetsTopicResponse topicData =
                    new ListOffsetsResponseData.ListOffsetsTopicResponse()
                    .setName(topic);
            responseData.topics().add(topicData);

            for (Map.Entry<TopicPartition, ListOffsetRequestV0.PartitionData> entry : offsetData.entrySet()) {
                TopicPartition topicPartition = entry.getKey();
                if (topicPartition.topic().equals(topic)) {
                    if (versionId == 0) {
                        topicData.partitions().add(new ListOffsetsResponseData.ListOffsetsPartitionResponse()
                                .setPartitionIndex(topicPartition.partition())
                                .setErrorCode(errors.code())
                                .setOldStyleOffsets(Collections.emptyList()));
                    } else {
                        topicData.partitions().add(new ListOffsetsResponseData.ListOffsetsPartitionResponse()
                                .setPartitionIndex(topicPartition.partition())
                                .setErrorCode(errors.code())
                                .setOffset(-1)
                                .setTimestamp(-1)
                                .setLeaderEpoch(-1));
                    }
                }
            }
        });


        switch (versionId) {
            case 0:
            case 1:
            case 2:
            case 3:
                return new ListOffsetsResponse(responseData);
            default:
                throw new IllegalArgumentException(
                        String.format("Version %d is not valid. Valid versions for %s are 0 to %d",
                        versionId, this.getClass().getSimpleName(), ApiKeys.LIST_OFFSETS.latestVersion()));
        }
    }

    public int replicaId() {
        return replicaId;
    }

    public IsolationLevel isolationLevel() {
        return isolationLevel;
    }

    @Deprecated
    public Map<TopicPartition, ListOffsetRequestV0.PartitionData> offsetData() {
        return offsetData;
    }

    public Map<TopicPartition, Long> partitionTimestamps() {
        return partitionTimestamps;
    }

    public Set<TopicPartition> duplicatePartitions() {
        return duplicatePartitions;
    }

    public static ListOffsetRequestV0 parse(ByteBuffer buffer, short version) {
        try {
            ListOffsetsRequest encodedRequest = (ListOffsetsRequest)
                    AbstractRequest.parseRequest(ApiKeys.LIST_OFFSETS, version, buffer).request;

            int replicaId = encodedRequest.replicaId();
            IsolationLevel isolationLevel = encodedRequest.isolationLevel();
            short requestVersion = encodedRequest.version();

            Map<TopicPartition, ListOffsetRequestV0.PartitionData> targetTimes = new HashMap<>();
            encodedRequest.topics().forEach(topic -> {
                topic.partitions().forEach(partitionData -> {
                    targetTimes.put(new TopicPartition(topic.name(), partitionData.partitionIndex()),
                            new ListOffsetRequestV0.PartitionData(partitionData.timestamp(),
                                    partitionData.maxNumOffsets()));
                });
            });

            return new ListOffsetRequestV0(replicaId, targetTimes,
                    isolationLevel, requestVersion, encodedRequest.data());
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }
    public static Schema[] schemaVersions() {
        return new Schema[] {LIST_OFFSET_REQUEST_V0, LIST_OFFSET_REQUEST_V1, LIST_OFFSET_REQUEST_V2,
                LIST_OFFSET_REQUEST_V3};
    }

    public static <T> Map<String, Map<Integer, T>> groupDataByTopic(Map<TopicPartition, ? extends T> data) {
        Map<String, Map<Integer, T>> dataByTopic = new HashMap<>();
        for (Map.Entry<TopicPartition, ? extends T> entry: data.entrySet()) {
            String topic = entry.getKey().topic();
            int partition = entry.getKey().partition();
            Map<Integer, T> topicData = dataByTopic.computeIfAbsent(topic, k -> new HashMap<>());
            topicData.put(partition, entry.getValue());
        }
        return dataByTopic;
    }

}
