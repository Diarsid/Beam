/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static diarsid.beam.core.base.util.EnumUtils.argMatchesEnum;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_DIRECTORY;

/**
 *
 * @author Diarsid
 */
public class EnumUtilsTest {
    
    public EnumUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void argMatchesEnum_webdir_WEB_DIRECTORY() {
        assertThat(argMatchesEnum("webdir", WEB_DIRECTORY), equalTo(true));
    }
    
    @Test
    public void argMatchesEnum_webdirect_WEB_DIRECTORY() {
        assertThat(argMatchesEnum("webdirect", WEB_DIRECTORY), equalTo(true));
    }
    
    @Test
    public void argMatchesEnum_dir_WEB_DIRECTORY() {
        assertThat(argMatchesEnum("dir", WEB_DIRECTORY), equalTo(true));
    }
    
    @Test
    public void argMatchesEnum_webdrictory_WEB_DIRECTORY() {
        assertThat(argMatchesEnum("webdrictory", WEB_DIRECTORY), equalTo(true));
    }
    
    @Test
    public void argMatchesEnum_wdrictory_WEB_DIRECTORY() {
        assertThat(argMatchesEnum("wdrictory", WEB_DIRECTORY), equalTo(true));
    }
    
    @Test
    public void argMatchesEnum_wdrictry_WEB_DIRECTORY() {
        assertThat(argMatchesEnum("webdrictry", WEB_DIRECTORY), equalTo(true));
    }
    
    @Test
    public void argMatchesEnum_panel_WEBPANEL() {
        assertThat(argMatchesEnum("panel", WEBPANEL), equalTo(true));
    }
    
}
