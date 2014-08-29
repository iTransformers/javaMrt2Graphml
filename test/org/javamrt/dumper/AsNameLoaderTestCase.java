package org.javamrt.dumper;

import junit.framework.Assert;
import org.junit.Test;

public class AsNameLoaderTestCase {
    @Test
    public void testParseLine(){
        AsNameLoader.ASName asName = AsNameLoader.parseLine("54226\tSUNY-OPTOMETRY - SUNY College of Optometry,US");
        Assert.assertEquals("54226",asName.getId());
        Assert.assertEquals("SUNY-OPTOMETRY",asName.getName());
        Assert.assertEquals("SUNY College of Optometry",asName.getDescription());
        Assert.assertEquals("US",asName.getCountry());
    }
    @Test
    public void testParseLine2(){
        AsNameLoader.ASName asName = AsNameLoader.parseLine("561\t3COM-A - 3Com Corporation,US");
        Assert.assertEquals("561",asName.getId());
        Assert.assertEquals("3COM-A",asName.getName());
        Assert.assertEquals("3Com Corporation",asName.getDescription());
        Assert.assertEquals("US",asName.getCountry());
    }
    @Test
    //197787  -Reserved AS-,ZZ
    public void testParseLine3(){
        AsNameLoader.ASName asName = AsNameLoader.parseLine("197787  -Reserved AS-,ZZ");
        Assert.assertEquals("197787",asName.getId());
        Assert.assertEquals("AS197787",asName.getName());
        Assert.assertEquals("Reserved AS-",asName.getDescription());
        Assert.assertEquals("ZZ",asName.getCountry());
    }


}
