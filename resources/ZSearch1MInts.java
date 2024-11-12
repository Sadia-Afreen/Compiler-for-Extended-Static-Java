class ListM{
    public int[] l;
    public boolean found;
}
public class ZSearch1MInts {
    public static void main(String[] args)
    {
        ListM lm;

        lm = new ListM();
        lm.l = new int[1000000];
        ZSearch1MInts.populate(lm.l);
        if(ZSearch1MInts.searchfor2(lm.l)){
            lm.found = true;
        }
        else{
            lm.found = false;
        }

    }

    static boolean searchfor2(int[] list){
        int i;
        i=0;
        while(i < 1000000){
            if(list[i] == 2){
                return true;
            }
            i++;
        }
        return false;
    }

    static void populate(int[] list)
    {
        int i;
        for(i=0; i<1000000; i++)
        {
            list[i] = i;
        }
    }

}