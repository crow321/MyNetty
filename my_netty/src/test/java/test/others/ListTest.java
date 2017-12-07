package test.others;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Created by zhangp on 2017/9/22.
 * @version v1.0.0
 */
public class ListTest {
    List<Long> list = new ArrayList<>();
    int length;

    @Test
    public void testBuffer() {

        addList(100L);
        addList(101L);
        addList(102L);
        addList(103L);
        addList(104L);
        addList(105L);


        for (int i = 0; i < length; i++) {
            long value = list.get(i);
            if (value == 102L) {
                remove(value);
            } else {
                if (value > 104) {
                    value = 104;
                }
            }
        }
        System.out.println("==============> list: " + list);
    }

    private void addList(long value) {
        list.add(value);
        length++;
    }

    private void remove(long id) {
        Iterator<Long> it = list.iterator();
        while (it.hasNext()) {
            if (id == it.next()) {
                it.remove();
                length--;
            }
        }
    }

}
