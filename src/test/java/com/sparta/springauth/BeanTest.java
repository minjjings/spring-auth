package com.sparta.springauth;

import com.sparta.springauth.food.Chicken;
import com.sparta.springauth.food.Food;
import com.sparta.springauth.food.Pizza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BeanTest {

    //Autowired는 Food의 타입으로 빈을 먼저 찾음 , 그 다음에 이름을 찾음!
    // 우리 식당에서 95% 의 주문율 차지하면 치킨 쪽에 Primary를 걸어준다.
    // 피자가 5%일 때 Qualifier 어노테이션을 걸어준다.
    // 좁은 범위의 우선순위가 높음
    @Autowired
    @Qualifier("pizza")
    Food food;

    @Test
    @DisplayName("테스트")
    void test1() {
        food.eat();

    }

}
