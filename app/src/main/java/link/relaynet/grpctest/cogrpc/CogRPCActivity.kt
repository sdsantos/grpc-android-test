package link.relaynet.grpctest.cogrpc

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.protobuf.ByteString
import io.grpc.*
import io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import link.relaynet.grpctest.R
import kotlin.random.Random

class CogRPCActivity : AppCompatActivity() {

    private var server: Server? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        server = NettyServerBuilder.forPort(8765)
            .addService(implementation)
            .maxInboundMessageSize(10_000_000)
            .maxInboundMetadataSize(10_000_000)
            .useTransportSecurity(
                assets.open("cert.pem"),
                assets.open("key.pem")
            )
            .intercept(interceptor)
            .build()
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.shutdown()
    }

    private val interceptor = object : ServerInterceptor {
        override fun <ReqT : Any?, RespT : Any?> interceptCall(
            call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>
        ): ServerCall.Listener<ReqT> {
            val auth =
                headers[Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)]
            val context = Context.current().withValue(CONTEXT_AUTH_KEY, auth)
            val previousContext = context.attach()
            return try {
                next.startCall(call, headers)
            } finally {
                context.detach(previousContext)
            }
        }
    }

    private val implementation = object : CargoRelayGrpc.CargoRelayImplBase() {
        override fun deliverCargo(responseObserver: StreamObserver<CargoDeliveryAck>): StreamObserver<CargoDelivery> {
            Log.i("CogRPC", "deliverCargo")

            return object : StreamObserver<CargoDelivery> {
                override fun onNext(value: CargoDelivery) {
                    Log.i("CogRPC", "Cargo collected ${value.id}")
                    responseObserver.onNext(
                        CargoDeliveryAck.newBuilder().setId(value.id).build()
                    )
                }

                override fun onError(t: Throwable) {
                    Log.w("CogRPC", "deliverCargo error", t)
                }

                override fun onCompleted() {
                    Log.i("CogRPC", "deliverCargo complete")
                    responseObserver.onCompleted()
                }
            }
        }

        override fun collectCargo(responseObserver: StreamObserver<CargoDelivery>): StreamObserver<CargoDeliveryAck> {
            Log.i("CogRPC", "collectCargo")
            val auth = CONTEXT_AUTH_KEY.get()
            Log.i("CogRPC", "with auth: ${auth.length}")

            val deliveries = listOf(
                CargoDelivery.newBuilder()
                    .setId(Random.nextInt().toString())
                    .setCargo(ByteString.copyFromUtf8("Hello World!"))
                    .build()
            )

            deliveries.forEach {
                responseObserver.onNext(it)
            }
            responseObserver.onCompleted()

            return object : StreamObserver<CargoDeliveryAck> {
                override fun onNext(value: CargoDeliveryAck) {
                    Log.i("CogRPC", "Cargo Delivered ${value.id}")
                }

                override fun onError(t: Throwable) {
                    Log.w("CogRPC", "Received error from client: ", t)
                }

                override fun onCompleted() {
                    Log.i("CogRPC", "All Done!")
                }
            }
        }
    }

    companion object {
        val CONTEXT_AUTH_KEY = Context.key<String>("Authorization")
    }
}
