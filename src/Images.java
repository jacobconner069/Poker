import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public final class Images {

    public static final int CARD_W = 70;
    public static final int CARD_H = 100;
    public static final int SMALL_CARD_W = 50;
    public static final int SMALL_CARD_H = 72;
    public static final int CHIP_SIZE = 28;
    private static final Map<String, ImageIcon> CACHE = new HashMap<>();
    
    private Images(){}

    public static ImageIcon card(String card){
        return scaled("/cards/" + card + ".png", CARD_W, CARD_H);
    }

    public static ImageIcon cardBack(){
        return card("BACK");
    }

    public static ImageIcon cardSmall(String card){
        return scaled("/cards/"+card+".png", SMALL_CARD_W, SMALL_CARD_H);
    }

    public static ImageIcon cardBackSmall(){
        return cardSmall("BACK");
    }

    public static ImageIcon chip(String color){
        return scaled("/chips/"+color+".png", CHIP_SIZE, CHIP_SIZE);
    }

    public static ImageIcon scaled(String path, int w, int h){
        String key = path + "@" + w + "x" + h;
        ImageIcon cached = CACHE.get(key);
        if(cached != null){
            return cached;
        }
        URL url = Images.class.getResource(path);
        if(url==null){
            System.err.println("Could not find resource: " + path);
            return new ImageIcon();
        }
        Image img = new ImageIcon(url).getImage();
        ImageIcon icon = new ImageIcon(img.getScaledInstance(w,h,Image.SCALE_SMOOTH));
        CACHE.put(key, icon);
        return icon;
    }
}
