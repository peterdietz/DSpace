/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.general;

import org.apache.log4j.Logger;
import org.dspace.app.mediafilter.LitImageMagickThumbnailFilter;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Item;
import org.dspace.curate.Curator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * MediaFilter Curation Task to generate a thumbnail for a PDF, makes use of REAL media filter
 * @author peterdietz
 */
public class ImageMagickPDFThumbnail extends MediaFilter {
    private static final Logger log = Logger.getLogger(ImageMagickPDFThumbnail.class);

    // supported input formats
    private List<String> mimeTypes = Arrays.asList("application/pdf");
    private List<String> suffixes = Arrays.asList("pdf");

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        super.init(curator, taskId);
    }

    @Override
    protected boolean canFilter(Item item, Bitstream bitstream) {
        BitstreamFormat bitstreamFormat = bitstream.getFormat();
        if (mimeTypes.contains(bitstreamFormat.getMIMEType())) {
            return true;
        }
        // now grovel thru the file suffixes
        for (String suffix : bitstreamFormat.getExtensions()) {
            if (suffixes.contains(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean filterBitstream(Item item, Bitstream bitstream) throws AuthorizeException, IOException, SQLException {
        {
            try {
                //Leverage the existing MediaFilter to do the heavy lifting.
                File f = LitImageMagickThumbnailFilter.inputStreamToTempFile(bitstream.retrieve(), "litpdfthumb", ".pdf");
                File f2 = LitImageMagickThumbnailFilter.getImageFile(f, 0);
                File f3 = LitImageMagickThumbnailFilter.getThumbnailFile(f2);
                return createDerivative(item, bitstream, new FileInputStream(f3));
            } catch (Exception e) {
                log.error("Error during PDF Thumb", e);
                return false;
            }
        }
    }
}
