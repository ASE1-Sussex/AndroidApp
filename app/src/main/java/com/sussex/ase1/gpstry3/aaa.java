package com.sussex.ase1.gpstry3;

/**
 * Created by User on 09/11/2016.
 */

public class aaa {
    /*package com.sussex.ase1.gpstry3;

    import org.junit.Before;
    import org.junit.Test;

    import static org.junit.Assert.assertTrue;
    import com.sussex.ase1.gpstry3.MainActivity;*/


    /**
     * Created by User on 08/11/2016.
     */

/*    public class MainActivityTest {

        private MainActivity mainActivity;

        MainActivityTest() {
            super(MainActivity.class);
        }

        @Before
        public void setUp() {
            super.setUp();
            mainActivity = setupActivity(MainActivity.class);
        }

        @Test
        public void validPostcode() throws Exception {
            String[] goodPostcodes = {"BN7 2AZ", "TN22 1QW", "BN3 3WD"};
            String[] badPostcodes = {"QN1 3AZ", "BN275 1A", "ZZ12 3AX"};

            for (int i = 0; i < goodPostcodes.length; i++) {

                String[] pArray = goodPostcodes[i].toUpperCase().trim().split(" ");
                String areaDistrict = pArray[0];

                String sectorUnit = "";
                if (pArray.length == 2)
                    sectorUnit = pArray[1];

                String pMatch = "";

                switch (areaDistrict.length()) {
                    case 1:
                        pMatch = "[A-Z&&[^QVX]]";
                        break;
                    case 2:
                        pMatch = "([A-Z&&[^QVX]][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]])";
                        break;
                    case 3:
                        pMatch = "(([A-Z&&[^QVX]][0-9][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9])|([A-Z&&[^QVX]][0-9][A-HJKPSTUW]))";
                        break;
                    case 4:
                        pMatch = "(([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9][ABEHMNPRVWXY]))";
                        break;
                    default:
                        ;
                }

                assertTrue(areaDistrict.matches(pMatch));

                switch (sectorUnit.length()) {
                    case 1:
                        pMatch = "[0-9]";
                        break;
                    case 2:
                        pMatch = "([0-9][A-Z&&[^CIKMOV]])";
                        break;
                    case 3:
                        pMatch = "([0-9][A-Z&&[^CIKMOV]]{2})";
                        break;
                    default:
                        ;
                }

                assertTrue(sectorUnit == "" | sectorUnit.matches(pMatch));
            }

            assertTrue(mainActivity.validPostcode("name@email.com") == );

        }

    }

        assertThat(EmailValidator.isValidEmail("name@email.com"), is(true));*/

}
