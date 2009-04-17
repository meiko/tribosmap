package org.tribosmap.model.business.service

import java.util.Date
import java.io.{File, FileInputStream}
import org.xml.sax.InputSource

import org.tribosmap.model.business.domain.{GeoMap, GeoReferencedPixel}
import org.tribosmap.model.business.repository.DAOAccessor
import org.tribosmap.model.math.geographic.{Position, UtmCoOrdinates, Hemisphere, Datum}
import org.tribosmap.model.math._
import org.tribosmap.model.app.AndroidXmlFactoryAdapter

/**
 * this is the component of the GeoMapService,
 * it is mixed in the IServiceRegistry,
 * it needs access to a GeoMapDAO and GeoReferencedPixelDAO
 * @author Meiko Rachimow
 */
trait GeoMapServiceComponent { 
  
  /**
   * the service to work with GeoMap objects
   */
  protected val mapService: GeoMapService  

  private[model] val daoAccessor: DAOAccessor

  /**
   * The service to work with a GeoMap
   * to use this class, you have to mix in the trait GeoMapServiceComponent
   * and define the repositories...
   */
  protected[service] class GeoMapService {
    
    /**
     * the access to the GeoMap repository
     */
    private[this] val geoMapDAO  = daoAccessor.geoMapDAO
    
    /**
     * the access to the GeoReferencedPixel repository
     */
    private[this] val geoReferencedPixelDAO = 
      daoAccessor.geoReferencedPixelDAO
  
    /** 
     * save a GeoMap to the repository
     * @param map the GeoMap to save it in the repository
     * @return if saved true else false
     */
    def save(map: GeoMap) = {
      val saved = if(map.id < 0) {
        geoMapDAO.create(map) != null
      } else {
        geoMapDAO.update(map)
      }
      assert(saved, 
             "cannot save the map in the repository: " + map.getName)
      saved
    }	
    
    /** 
     * delete a GeoMap in the repository
     * @param map the GeoMap to delete
     * @return if deleted true else false
     */
    def delete(map: GeoMap) = {
      val deleted = geoMapDAO.delete(map)
      assert(deleted, 
             "cannot delete the map from the repository: " + map.getName)
      deleted
    }
  
    /**
     * get all GeoReferencedPixels belonging to a given GeoMap
     * @param map the GeoMap
     * @return a list of all GeoReferencedPixels of the map
     */
    def getAllReferencedPixelsForMap(map: GeoMap) = {
      geoReferencedPixelDAO.fetchAll(map)
    }
    
    /**
     * call a function on every GeoMap immediatly,
     * when the object is fetched from the repository
     * 
     * @param fun the function to call
     */
    def foreachMap(fun: (GeoMap) ⇒ Unit) = 
      geoMapDAO.foreach(fun) 
    
    /**
     * load the map from the repository, 
     * will return a map with new actual content.
     * 
     * @param map the map to reload
     * @return the new map
     */
    def reloadMap(map: GeoMap) = {
      geoMapDAO.fetch(map.id)
    }
    
    /**
     * import a map from an xml file (the format of this file must have the tribosmap-xml-map format)
     * @param file the tribosmap xml File to import
     * @param timeOfCreation an unix timestamp which will be saved saved in the map
     * @return the created map
     */
    def importMapFromXml(file: File, timeOfCreation: Long) = {
      //val rootNode = scala.xml.XML.loadFile(file)
      val rootNode = new AndroidXmlFactoryAdapter().loadXML(
        new InputSource(new FileInputStream(file)))

      val mapName = rootNode.attribute("name").get.text

      var imageFileName: Option[String] = None
      var datum: Option[String] = None
      var projection: Option[String] = None
      var size: Option[Vector2i] = None
      var points: Option[Seq[GeoReferencedPixel]] = None
      
      for(child ← rootNode.child) child.label match {
          case "file" ⇒ 
            val imageFile = new File(file.getParentFile, child.text)
            //assert(imageFile.exists, "mapFile " + imageFile + " does not exists")
            imageFileName = Some(imageFile.getAbsolutePath)
            
          case "datum" ⇒ 
            datum = Some(child.text)
            
          case "projection" ⇒ 
            projection = Some(child.text)
            
          case "size" ⇒ 
            size = Some(Vector2i(
              child.attribute("x").get.text.toInt,
              child.attribute("y").get.text.toInt
            ))
            
          case "points" ⇒ 
            points = Some(
              for(point ← child.child.filter(_.label equals "point")) yield {

                var utm: Option[UtmCoOrdinates] = None

                val pixel = Some(Vector2i(
                  point.attribute("x").get.text.toInt,
                  point.attribute("y").get.text.toInt
                ))
                  
                point.child.foreach(pos ⇒ {
                  pos.label match {
                    case "utm" ⇒ 
                      utm = Some(UtmCoOrdinates(
                        pos.attribute("northing").get.text.toDouble,
                        pos.attribute("easting").get.text.toDouble,
                        pos.attribute("zonenumber").get.text.toInt,
                        Hemisphere(pos.attribute("hemisphere").get.text),
                        Preferences.standardDatum
                      ))
                    case "latLon" ⇒ 
                      error("Import with Latitude / Longitude coordinates not supported now!")
                      /*
                      Some(GeographicCoOrdinatePair(
                        pos.attribute("latitude").get.text.toDouble,
                        pos.attribute("longitude").get.text.toDouble
                      ))
                      */
                    case _ ⇒
                  }		
                })
                assume(pixel.isDefined, "no pixel was defined in the xml")
                assume(utm.isDefined, "no utm coord was defined in the xml")           
                new GeoReferencedPixel(-1, -1, pixel.get, utm.get)

              })
          case _ ⇒
      }

      assume(imageFileName.isDefined, "no imageFileName was defined in the xml")
      assume(datum.isDefined, "no datum was defined in the xml")
      assume(projection.isDefined, "no projection was defined in the xml")
      assume(size.isDefined, "no size of the image was defined in the xml")
      assume(points.isDefined && points.get.length > 1, "less than 2 points are defined in the xml")

      val refPixels = points.get
      val map = geoMapDAO.create(
        new GeoMap(-1l, mapName, imageFileName.get, size.get, Datum(datum.get), 
                   projection.get, timeOfCreation))
      
      refPixels.foreach(refPixel ⇒ geoReferencedPixelDAO.create(
        new GeoReferencedPixel(-1, map.id, refPixel.pixel, refPixel.utm)
      ))
      
      map
      
    } 
  }
  
    /**
   * implicit definition, for GeoMap objects
   */
  protected implicit def doOnMap(map: GeoMap) = new {
    
    /** 
     * save the GeoMap to the repository
     * @return if saved true else false
     */
    def save() = mapService.save(map)
    
    /** 
     * delete the GeoMap in the repository
     * @return if deleted true else false
     */
    def delete() = mapService.delete(map)
    
    /**
     * get all GeoReferencedPixels belonging to a given GeoMap
     * @return a list of all GeoReferencedPixels of the map
     */
    def getAllReferencedPixels = mapService.getAllReferencedPixelsForMap(map)
    
    /**
     * load the map from the repository, 
     * will return a map with new actual content.
     * @return the new map
     */
    def reload() = mapService.reloadMap(map)
  }
}	