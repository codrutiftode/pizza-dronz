package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.api.OrderValidator;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class OrderValidatorTest extends TestCase {

    private Restaurant[] getDemoRestaurants() {
        DayOfWeek[] daysOpen1 = new DayOfWeek[]{
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        };
        DayOfWeek[] daysOpen2 = new DayOfWeek[]{
                LocalDate.now().getDayOfWeek().plus(1)
        };
        return new Restaurant[]{
                new Restaurant("Dominos", new LngLat(10, 10), daysOpen1, new Pizza[]{
                        new Pizza("Margherita", 100),
                        new Pizza("Quatro Staggioni", 200)
                }),
                new Restaurant("Papa Johns", new LngLat(20, 20), daysOpen2, new Pizza[]{
                        new Pizza("Quatro Formaggi", 300),
                        new Pizza("Pineapple", 150),
                        new Pizza("Diablo", 250)
                }),
        };
    }

    private CreditCardInformation getDemoCreditCard() {
        return new CreditCardInformation("1234567890123456", "10/26", "123");
    }

    public void testCardExpiryCorrect() {
        OrderValidator validator = new OrderValidator();
        Order order = new Order();
        order.setPriceTotalInPence(300);
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Margherita", 100),
                new Pizza("Quatro Staggioni", 200)
        });
        CreditCardInformation cardInfo = getDemoCreditCard();
        String newExpiry = String.format("%2d/%2d", LocalDate.now().getMonth().minus(1).getValue(), LocalDate.now().getYear() % 100);
        cardInfo.setCreditCardExpiry(newExpiry);
        order.setCreditCardInformation(cardInfo);
        Order validatedOrder = validator.validateOrder(order, getDemoRestaurants());
        assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, validatedOrder.getOrderValidationCode());
    }

    public void testTotalPriceCorrect() {
        OrderValidator validator = new OrderValidator();
        Order order = new Order();
        order.setPriceTotalInPence(300 + SystemConstants.ORDER_CHARGE_IN_PENCE);
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Margherita", 100),
                new Pizza("Quatro Staggioni", 200)
        });
        order.setCreditCardInformation(getDemoCreditCard());
        Order validatedOrder = validator.validateOrder(order, getDemoRestaurants());
        assertNotSame(OrderValidationCode.TOTAL_INCORRECT, validatedOrder.getOrderValidationCode());
    }

    public void testTotalPriceIncorrect() {
        OrderValidator validator = new OrderValidator();
        Order order = new Order();
        order.setPriceTotalInPence(301);
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Margherita", 100),
                new Pizza("Quatro Staggioni", 200)
        });
        order.setCreditCardInformation(getDemoCreditCard());
        Order validatedOrder = validator.validateOrder(order, getDemoRestaurants());
        assertEquals(OrderValidationCode.TOTAL_INCORRECT, validatedOrder.getOrderValidationCode());
    }

    public void testPizzaNotDefined() {
        OrderValidator validator = new OrderValidator();
        Order order = new Order();
        order.setPriceTotalInPence(300 + SystemConstants.ORDER_CHARGE_IN_PENCE);
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Margherita", 100),
                new Pizza("Quatro Staggionii", 200)
        });
        order.setCreditCardInformation(getDemoCreditCard());
        Order validatedOrder = validator.validateOrder(order, getDemoRestaurants());
        assertEquals(OrderValidationCode.PIZZA_NOT_DEFINED, validatedOrder.getOrderValidationCode());
    }

    public void testPizzaMaxCountExceeded() {
        OrderValidator validator = new OrderValidator();
        Order order = new Order();
        order.setPriceTotalInPence(500 + SystemConstants.ORDER_CHARGE_IN_PENCE);
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Margherita", 100),
                new Pizza("Margherita", 100),
                new Pizza("Margherita", 100),
                new Pizza("Margherita", 100),
                new Pizza("Margherita", 100),
        });
        order.setCreditCardInformation(getDemoCreditCard());
        Order validatedOrder = validator.validateOrder(order, getDemoRestaurants());
        assertEquals( OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED, validatedOrder.getOrderValidationCode());
    }

    public void testPizzaMultipleRestaurants() {
        OrderValidator validator = new OrderValidator();
        Order order = new Order();
        order.setPriceTotalInPence(400 + SystemConstants.ORDER_CHARGE_IN_PENCE);
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Margherita", 100),
                new Pizza("Pineapple", 200),
                new Pizza("Diablo", 100),
        });
        order.setCreditCardInformation(getDemoCreditCard());
        Order validatedOrder = validator.validateOrder(order, getDemoRestaurants());
        assertEquals(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS, validatedOrder.getOrderValidationCode());
    }

    public void testRestaurantClosed() {
        OrderValidator validator = new OrderValidator();
        Order order = new Order();
        order.setPriceTotalInPence(450 + SystemConstants.ORDER_CHARGE_IN_PENCE);
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Quatro Formaggi", 300),
                new Pizza("Pineapple", 150),
        });
        order.setOrderDate(LocalDate.now());
        order.setCreditCardInformation(getDemoCreditCard());
        Order validatedOrder = validator.validateOrder(order, getDemoRestaurants());
        assertEquals(OrderValidationCode.RESTAURANT_CLOSED, validatedOrder.getOrderValidationCode());
    }
}
