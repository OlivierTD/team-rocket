package dummy;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Sai Shan on 2018-02-12.
 */

public class DummyTestSaiShan {

    @Test
    public void testSai(){

        String str = "testing";
        String s = "test";
        String t = "ing";

        s = s.concat(t);

        assertEquals(str, s);
    }
}
