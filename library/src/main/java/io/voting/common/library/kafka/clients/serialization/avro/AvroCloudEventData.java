package io.voting.common.library.kafka.clients.serialization.avro;

import io.cloudevents.CloudEventData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.avro.generic.IndexedRecord;

import java.util.Objects;

@ToString
@EqualsAndHashCode
@Getter
public class AvroCloudEventData<T extends IndexedRecord> implements CloudEventData {

  public static final String MIME_TYPE = "application/avro";

  public final T value;
  public AvroCloudEventData(final T value){
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public byte[] toBytes() {
    return new byte[]{};
  }

  @SuppressWarnings("unchecked")
  public static <V extends IndexedRecord> V dataOf(CloudEventData data) {

    if(data instanceof AvroCloudEventData){

      return ((AvroCloudEventData<V>)data).getValue();

    } else {
      throw new IllegalArgumentException("data argument must be an instance of "
              + AvroCloudEventData.class.getName());
    }
  }
}
