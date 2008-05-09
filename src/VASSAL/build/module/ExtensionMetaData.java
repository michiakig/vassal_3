/* $Id$
 *
 * Copyright (c) 2008 by Brent Easton and Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.build.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import VASSAL.build.GameModule;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.ErrorLog;

public class ExtensionMetaData extends AbstractMetaData {

  public static final String ZIP_ENTRY_NAME = "extensiondata";
  public static final String DATA_VERSION = "1";

  protected static final String UNIVERSAL_ELEMENT = "universal";
  protected static final String UNIVERSAL_ATTR = "anyModule";

  protected ModuleMetaData moduleData;
  protected boolean universal;

  /**
   * Build an ExtensionMetaData for the given extension
   * 
   * @param ext
   *          Extension
   */
  public ExtensionMetaData(ModuleExtension ext) {
    super();
    setVersion(ext.getVersion());
    setDescription(
      new Attribute(ModuleExtension.DESCRIPTION, ext.getDescription()));
    universal = ext.getUniversal();
  }

  /**
   * Read Extension metadata from specified zip archive
   * 
   * @param zip
   *          Archive
   */
  public ExtensionMetaData(ZipFile zip) {
    read(zip);
  }

  public String getModuleName() {
    return moduleData == null ? "" : moduleData.getName();
  }
  
  public String getModuleVersion() {
    return moduleData == null ? "" : moduleData.getVersion();
  }

  public String getZipEntryName() {
    return ZIP_ENTRY_NAME;
  }
  
  public String getMetaDataVersion() {
    return DATA_VERSION;
  }
 
  /**
   * Write Extension metadata to the specified Archive
   * @param archive Save game Archive
   * @throws IOException If anything goes wrong
   */
  public void save(ArchiveWriter archive) throws IOException {
    super.save(archive);
    
    // Also save a copy of the current module metadata in the save file. Copy
    // module metadata from the module archive as it will contain full i18n
    // information.
    copyModuleMetadata(archive);
  }
 
  /**
   * Add elements specific to an ExtensionMetaData 
   * 
   * @param doc Document
   * @param root Root element
   */
  protected void addElements(Document doc, Element root) {
    final Element e = doc.createElement(UNIVERSAL_ELEMENT);
    e.appendChild(doc.createTextNode(String.valueOf(universal)));
    root.appendChild(e);
  }
  
  /**
   * Read and validate an Extension file.
   *  - Check it has a Zip Entry named buildfile
   *  - If it has a metadata file, read and parse it.
   *  
   * @param file Module File
   */
  public void read(ZipFile zip) {
    InputStream is = null;
    try {

      // Try to parse the metadata. Failure is not catastrophic, we can
      // treat it like an old-style module with no metadata and parse
      // the first lines of the buildFile
      try {
        final XMLReader parser = XMLReaderFactory.createXMLReader();
        DefaultHandler handler = null;
        ZipEntry data = zip.getEntry(getZipEntryName());
        if (data == null) {
          data = zip.getEntry(GameModule.BUILDFILE);
          handler = new ExtensionBuildFileXMLHandler();
        }
        else {
          handler = new MetadataXMLHandler();
        }
        
        parser.setContentHandler(handler);
        parser.setDTDHandler(handler);
        parser.setEntityResolver(handler);
        parser.setErrorHandler(handler);

        // parse! parse!
        is = zip.getInputStream(data);
        parser.parse(new InputSource(is));

        // read the matching Module data. A basic moduledata may have been
        // built when reading the buildFile, overwrite if we find a real
        // module metadata file
        final ModuleMetaData buildFileModuleData = moduleData;
        moduleData = new ModuleMetaData(zip); 
        if (moduleData == null) {
          moduleData = buildFileModuleData;
        }
      }
      catch (IOException e) {
        ErrorLog.log(e);
      }
      catch (SAXEndException e) {
        // Indicates End of module/extension parsing. not an error.
      }
      catch (SAXException e) {
        ErrorLog.log(e);
      }
    }
    finally {
      if (zip != null) {
        try {
          zip.close();
        }
        catch (IOException e) {
          ErrorLog.log(e);
        }
      }
      
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
          ErrorLog.log(e);
        }
      }
    }
  }
  
  /**
   * XML Handler for parsing an Extension metadata file
   */
  private class MetadataXMLHandler extends XMLHandler {
    
    @Override
    public void endElement(String uri, String localName, String qName) {
      // handle all of the elements which have CDATA here
      if (UNIVERSAL_ELEMENT.equals(qName)) {
        universal = "true".equals(accumulator.toString().trim());
      }
      else {
        super.endElement(uri, localName, qName);
      }
    }
  }
  
  /**
   * XML Handle for parsing an extension buildFile. Used to read minimal data from
   * extensions saved prior to 3.1.0. 
   */
  private class ExtensionBuildFileXMLHandler extends BuildFileXMLHandler {

    @Override
    public void startElement(String uri, String localName,
                             String qName, Attributes attrs) 
        throws SAXEndException {
      super.startElement(uri, localName, qName, attrs);

      // handle element attributes we care about
      if (BUILDFILE_EXTENSION_ELEMENT.equals(qName)) {
        setVersion(getAttr(attrs, VERSION_ATTR));
        setVassalVersion(getAttr(attrs, VASSAL_VERSION_ATTR));
        setDescription(getAttr(attrs, DESCRIPTION_ATTR));
        universal = "true".equals(getAttr(attrs, UNIVERSAL_ATTR));
        // Build a basic module metadata in case this is an older extension
        // with no metadata from the originating module
        final String moduleName = getAttr(attrs, MODULE_NAME_ATTR);
        final String moduleVersion = getAttr(attrs, MODULE_VERSION_ATTR);
        moduleData = new ModuleMetaData(moduleName, moduleVersion);
        throw new SAXEndException();
      }
    }
  }
}
