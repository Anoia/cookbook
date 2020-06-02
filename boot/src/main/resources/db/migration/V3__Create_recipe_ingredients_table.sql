CREATE TABLE recipe_ingredients
(
    recipe_id    INT REFERENCES recipe (recipe_id) ON UPDATE CASCADE ON DELETE CASCADE,
    foodstuff_id INT REFERENCES foodstuff (foodstuff_id) ON UPDATE CASCADE,
    amount       INT NOT NULL DEFAULT 1,
    CONSTRAINT recipe_ingredients_pkey PRIMARY KEY (recipe_id, foodstuff_id)
)