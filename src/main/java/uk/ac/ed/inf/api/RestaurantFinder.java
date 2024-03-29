package uk.ac.ed.inf.api;

import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.util.HashMap;

/**
 * Finds the corresponding restaurant for a valid order / pizza.
 */
public class RestaurantFinder {
    private final HashMap<String, Restaurant> pizzaToRestaurantMap;
    public RestaurantFinder(Restaurant[] restaurants) {
        this.pizzaToRestaurantMap = getPizzaToRestaurantMap(restaurants);
    }

    /**
     * Get the corresponding restaurant.
     * @param order the order
     * @return the corresponding restaurant
     */
    public Restaurant getRestaurantForOrder(Order order) {
        String firstPizza = order.getPizzasInOrder()[0].name();
        return this.pizzaToRestaurantMap.get(firstPizza);
    }

    /**
     * Get the corresponding restaurant.
     * @param pizza the pizza
     * @return the corresponding restaurant
     */
    public Restaurant getRestaurantForPizza(Pizza pizza) {
        return this.pizzaToRestaurantMap.get(pizza.name());
    }

    /**
     * Map each pizza name to a restaurant.
     * Note: assumes pizza names are unique, i.e. belong to only one restaurant.
     * @param restaurants the restaurants to include in the map.
     * @return a map containing a restaurant for every pizza name on the menus.
     */
    private HashMap<String, Restaurant> getPizzaToRestaurantMap(Restaurant[] restaurants) {
        HashMap<String, Restaurant> pizzaToRestaurant = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            Pizza[] pizzas = restaurant.menu();
            for (Pizza pizza : pizzas) {
                pizzaToRestaurant.put(pizza.name(), restaurant);
            }
        }
        return pizzaToRestaurant;
    }
}
