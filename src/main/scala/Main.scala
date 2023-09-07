//> using dep co.fs2::fs2-core:3.9.1
//> using dep co.fs2::fs2-io:3.9.1

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.MonadCancelThrow
import cats.effect.kernel.Async
import cats.effect.std.Console
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Chunk
import fs2.Stream
import fs2.io.net.Network
import fs2.io.net.tls.TLSContext
import fs2.io.stdinUtf8
import fs2.text

object Main extends IOApp.Simple {
  override def run: IO[Unit] = {
    val tlsCtx = Network[IO].tlsContext.systemResource
    tlsCtx
      .use { ctx =>
        koukokuChatClient(ctx).compile.drain
      }
  }
}

def koukokuChatClient[F[_]: MonadCancelThrow: Console: Network: Async](
    tlsContext: TLSContext[F]
): Stream[F, Unit] = {
  Stream
    .resource(
      Network[F]
        .client(SocketAddress(host"koukoku.shadan.open.ad.jp", port"992"))
    )
    .flatMap { rawSocket =>
      Stream.resource(tlsContext.client(rawSocket)).flatMap { socket =>
        val init = Stream.eval(
          Console[F].println("Connected!") >> socket
            .write(Chunk.array("nobody\n".getBytes()))
        )

        val send = stdinUtf8[F](1024)
          .through(text.lines)
          .map(_ ++ "\n")
          .through(text.utf8.encode)
          .through(socket.writes)

        val recv = socket.reads
          .through(text.utf8.decode)
          .through(text.lines)
          .foreach { response =>
            Console[F].println(response)
          }

        init ++ send.concurrently(recv)
      }
    }
}
