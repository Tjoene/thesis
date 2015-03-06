package geotrellis.raster.op.global

import geotrellis._

/**
 * Converts a raster to an integer array.
 */
case class AsArray(r:Op[Raster]) extends Op1(r)({ 
  r => 
    Result(r.toArray)
})

/**
 * Converts a raster to a double array.
 */
case class AsArrayDouble(r:Op[Raster]) extends Op1(r)({
  r =>  
    Result(r.toArrayDouble)
})
