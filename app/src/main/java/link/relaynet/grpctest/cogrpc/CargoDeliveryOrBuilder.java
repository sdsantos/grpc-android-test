// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: app/src/main/proto/cogrpc.proto

package link.relaynet.grpctest.cogrpc;

public interface CargoDeliveryOrBuilder extends
    // @@protoc_insertion_point(interface_extends:relaynet.cogrpc.CargoDelivery)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>bytes cargo = 2;</code>
   * @return The cargo.
   */
  com.google.protobuf.ByteString getCargo();
}
