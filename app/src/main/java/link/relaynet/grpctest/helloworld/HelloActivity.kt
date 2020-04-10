package link.relaynet.grpctest.helloworld

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import link.relaynet.grpctest.R


class HelloActivity : AppCompatActivity() {

    private var server: Server? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val impl = object : GreeterGrpc.GreeterImplBase() {
            override fun sayHello(
                request: HelloRequest,
                responseObserver: StreamObserver<HelloReply>
            ) {
                val reply = HelloReply.newBuilder().setMessage("Hello " + request.name).build()
                Log.i("GRPC", "sayHello ${request.name}")
                responseObserver.onNext(reply)
                responseObserver.onCompleted()
            }
        }

        server = NettyServerBuilder.forPort(8765)
            .addService(impl)
            .useTransportSecurity(
                assets.open("server.crt"),
                assets.open("server.key")
            )
            .build()
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.shutdown()
    }
}
