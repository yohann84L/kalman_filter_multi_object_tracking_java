import org.opencv.core.Point;
import org.opencv.core.Rect;

public class Counter {


    private static Counter instance;

    public static Counter getInstance(Rect rect) {
        if(instance == null) {
            instance = new Counter();
        }
        return instance;
    }

    private Counter() {
    }

    public boolean isInArea(Point pt, Rect rect) {
        return pt.x > rect.tl().x && pt.y > rect.tl().y &&  pt.x < rect.br().x && pt.y < rect.br().y;
    }

    public boolean isEnteringArea(Point pt1, Point pt2, Rect rect) {
        return !isInArea(pt1, rect) && isInArea(pt2, rect);
    }

    public boolean isCrossedLinesEntryArea(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "bottom");

        return pt1.y > line.getPt1().y && pt2.y < line.getPt1().y;
    }

    public boolean isCrossedLinesExitArea(Point pt1, Point pt2, Rect rect) {
        return isInArea(pt1, rect) && (pt2.x > rect.br().x || pt2.x < rect.tl().x || pt2.y < rect.tl().y);
    }

    public boolean crossBottomIn(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "bottom");
        return pt1.y > line.getPt1().y
                && pt2.y < line.getPt1().y
                && pt1.x > line.getPt1().x
                && pt2.x > line.getPt1().x
                && pt1.x < line.getPt2().x
                && pt2.x < line.getPt2().x;
    }

    public boolean crossBottomOut(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "bottom");
        return pt2.y > line.getPt1().y
                && pt1.y < line.getPt1().y
                && pt1.x > line.getPt1().x
                && pt2.x > line.getPt1().x
                && pt1.x < line.getPt2().x
                && pt2.x < line.getPt2().x;
    }

    public boolean crossTopOut(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "top");
        return pt1.y > line.getPt1().y
                && pt2.y < line.getPt1().y
                && pt1.x > line.getPt1().x
                && pt2.x > line.getPt1().x
                && pt1.x < line.getPt2().x
                && pt2.x < line.getPt2().x;
    }

    public boolean crossTopIn(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "top");
        return pt2.y > line.getPt1().y
                && pt1.y < line.getPt1().y
                && pt1.x > line.getPt1().x
                && pt2.x > line.getPt1().x
                && pt1.x < line.getPt2().x
                && pt2.x < line.getPt2().x;
    }

    public boolean crossLeftIn(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "left");
        return pt1.x < line.getPt1().x
                && pt2.x > line.getPt1().x
                && pt1.y > line.getPt1().y
                && pt2.y > line.getPt1().y
                && pt1.y < line.getPt2().y
                && pt2.y < line.getPt2().y;
    }

    public boolean crossLeftOut(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "left");
        return pt2.x < line.getPt1().x
                && pt1.x > line.getPt1().x
                && pt1.y > line.getPt1().y
                && pt2.y > line.getPt1().y
                && pt1.y < line.getPt2().y
                && pt2.y < line.getPt2().y;
    }

    public boolean crossRightOut(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "right");
        return pt1.x < line.getPt1().x
                && pt2.x > line.getPt1().x
                && pt1.y > line.getPt1().y
                && pt2.y > line.getPt1().y
                && pt1.y < line.getPt2().y
                && pt2.y < line.getPt2().y;
    }

    public boolean crossRightIn(Point pt1, Point pt2, Rect rect) {
        Line line = new Line(rect, "right");
        return pt2.x < line.getPt1().x
                && pt1.x > line.getPt1().x
                && pt1.y > line.getPt1().y
                && pt2.y > line.getPt1().y
                && pt1.y < line.getPt2().y
                && pt2.y < line.getPt2().y;
    }

    class Line {

         Point pt1;
         Point pt2;

         public Line(Point pt1, Point pt2) {
             this.pt1 = pt1;
             this.pt2 = pt2;
         }

         public Line(Rect rect, String side) {
             switch (side) {
                 case "bottom" :
                     pt1 = new Point(rect.tl().x, rect.br().y);
                     pt2 = rect.br();
                     break;
                 case "top" :
                     pt1 = rect.tl();
                     pt2 = new Point(rect.br().x, rect.tl().y);
                     break;
                 case "left" :
                    pt1 = rect.tl();
                    pt2 = new Point(rect.tl().x, rect.br().y);
                     break;
                 case "right" :
                     pt1 = new Point(rect.br().x, rect.tl().y);
                     pt2 = rect.br();
                     break;
             }
         }

        public Point getPt1() {
            return pt1;
        }

        public Point getPt2() {
            return pt2;
        }
    }
}
