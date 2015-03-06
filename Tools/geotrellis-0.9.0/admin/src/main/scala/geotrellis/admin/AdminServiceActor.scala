package geotrellis.admin

import geotrellis._
import geotrellis.process._
import geotrellis.source._
import geotrellis.services._
import geotrellis.util.srs

import akka.actor._
import spray.routing._
import spray.can.Http
import spray.http._

class AdminServiceActor(val staticContentPath:String) extends Actor with AdminService {
  def actorRefFactory = context
  def receive = 
    runRoute {
      get { pathSingleSlash { redirect("index.html",StatusCodes.Found) } } ~
      serviceRoute ~
      get { getFromDirectory(staticContentPath) }
    }
}

trait AdminService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  val staticContentPath:String

  lazy val serviceRoute =
    get {
      pathPrefix("gt") {
        path("catalog") {
          complete(CatalogService.getJson)
        } ~
        path("colors") {
          complete(ColorRampMap.getJson)
        } ~
        pathPrefix("layer") {
          layerRoute
        }
      }
    }

  lazy val layerRoute:Route =
    path("breaks") {
      parameters('store,'layer,'numBreaks.as[Int]) { (store,layer,numBreaks) =>
        LayerService.getBreaks(LayerId(store,layer),numBreaks).run match {
          case Complete(v,_) => complete(v)
          case Error(message,_) => failWith(new RuntimeException(message))
        }
      }
    } ~
    path("info") {
      parameters('store,'layer) { (store,layer) =>
        LayerService.getInfo(LayerId(store,layer)).run match {
          case Complete(v,_) => complete(v)
          case Error(message,_) => failWith(new RuntimeException(message))
        }
      }
    } ~ 
    path("bbox") {
      parameters('store,'layer) { (store,layer) =>
        LayerService.getBoundingBox(LayerId(store,layer)).run match {
          case Complete(v,_) => complete(v)
          case Error(message,_) => failWith(new RuntimeException(message))
        }
      }
    } ~
    path("render") {
      parameters(
        'bbox,
        'width.as[Int],
        'height.as[Int],
        'store,
        'layer,
        'breaks,
        'colorRamp) { (bbox,width,height,store,layer,breaks,colorRamp) =>
        val layerId = LayerId(store,layer)
        LayerService.render(bbox,width,height,layerId,breaks,colorRamp).run match {
          case Complete(v,_) => 
            respondWithMediaType(MediaTypes.`image/png`) {
              complete(v)
            }
          case Error(message,trace) => failWith(new RuntimeException(message+trace))
        }
      }
    } ~ 
    path("valuegrid") {
      parameters(
        'store,
        'layer,
        'lat.as[Double],
        'lng.as[Double],
        'size.as[Int] ? 7) { (store,layer,lat,lng,size) =>
        val layerId = LayerId(store,layer)
        val (x,y) = srs.LatLng.transform(lng,lat,srs.WebMercator)

        RasterSource(layerId)
          .converge
          .map { rast =>
            val (col,row) = rast.rasterExtent.mapToGrid(x,y)
            for(r <- (row - size) to (row + size);
              c <- (col - size) to (col + size)) yield {
              if(0 <= c && c <= rast.cols &&
                0 <= r && r <= rast.rows) {
                "\"%.2f\"".format(rast.getDouble(c,r))
              } else {
                "\"\""
              }
            }
          }
         .map { values =>
            s""" { "success" : "1", "values" : [ ${values.mkString(",")} ] } """
          }
         .run match {
            case Complete(v,_) => complete(v)
            case Error(message,_) => failWith(new RuntimeException(message))
          }
      }
    }
}
