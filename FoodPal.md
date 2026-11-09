i want to build a macronutrient and fitness tracker that is compatible with grapheneos. it must not use any of google proprietary libraries for development. it must be entirely compatible with grapheneos security requirements. it will be built to be open source, however we may in the future expand into encrypted storage for users if we find a feature that warrants it (perhaps data backup or device sync). the app will store logged data by day in a db on the device and will be viewable by the user, this data will be used to extrapolate various nutrient insights based on open sources such as open food facts

the UI style must be clean, dark and modern.

# sign up 

the sign up page should allow the user to create  a profile on the app and enter their data. the data requested should be nickname, age, height, weight, lifestyle (sedentry, moderate, active, very active), goals (gain muscle, lose body fat, improve cardio health), goal weight.

# page 1 - dashboard
the main dashboard should include a summary titled "Today" the summary will be made up of various cards including:
### card 1 -Calories 
total calories consumed, 
exercise calories, 
goal calories 

### card 2 -Macros
totals consumed for protein, carbs, fats - all calculated from user entries for food/meals that day. 

a bar graph for protein carbs and fats that shows how much of each macro's goal amount has been consumed, these amounts default to: protein 30% of total calories, carbs 40% of total caloric consumption. fats 30% of total caloric consumption. there should be a small button next to the bar chart titled "edit macros", on press this should take the user to the section on the settings page to adjust the goal macro percentage


## card 3 - micronutrients

written totals of a few key nutrients against the recommended daily amount. each of these should also have a bar that represent the amount consumed of the goal total for the day these nutrients should include and default to; 

Saturated Fat

32 g

Polyunsaturated Fat

0 g

Monounsaturated Fat

0 g

Trans Fat

0 g

Cholesterol

300 mg

Sodium

2300 mg

Potassium

3500 mg

Fiber

38 g

Sugar

108 g

Vitamin A

100 %DV

Vitamin C

100 %DV

Calcium

100 %DV

Iron

100 %DV

there should be a small button at the bottom of this card titles 'edit micronutrients', on press this should take the user to the section on the settings page to adjust the goal micronutrients percentage

# page 2 - food

this will be the page where the user inputs and logs meals and food

at the top will be a date carousel that will default to todays date, pressing the left arrow will display the previous day's date and logs from that date. pressing the right arrow will move to the next day and any logs stored for that day.

### card 1 - breakfast

this card will have a button titled 'search food', on press a search bar will appear that allows the user to search for foods in a hosted copy of the open food facts database, as well as other data that will be added to the hosted db for generic foods. once an item has been selected the user will be presented with two  drop down menus to select the amount of the food being added to the log, one for a number (1,10,5,1000 etc) and one for the unit (g, kg, oz, floz, ml, litre, etc). once confirmed thsi data will be saved in the users log for that day at breakfast.

this card will also include an 'add custom food' button, on press this will open the users custom food list, here they will be presented with a user created list from the 'my foods' page. if the my foods page is empty there will be a short description that reads "here you can add custom foods if you cannot find them in our database, eg 'my bread'...". below this will be a button titled 'add custom food'. if there are already food saved in the users 'my foods' page, these should be displayed here and the user can quick add them to the log for the date chosen on the carousel

this card will also include a quick 'actions button', on press the user will be given the option to 'copy yesterday' or 'copy from date'. these actions will copy the entirety of yesterdays breakfast log, or present the user with a date selector and once confirmed, copy the entirety of the breakfast log from the selected date

### card 2 - lunch

this card will have a button titled 'search food', on press a search bar will appear that allows the user to search for foods in a hosted copy of the open food facts database, as well as other data that will be added to the hosted db for generic foods. once an item has been selected the user will be presented with two  drop down menus to select the amount of the food being added to the log, one for a number (1,10,5,1000 etc) and one for the unit (g, kg, oz, floz, ml, litre, etc). once confirmed thsi data will be saved in the users log for that day at lunch.

this card will also include an 'add custom food' button, on press this will open the users custom food list, here they will be presented with a user created list from the 'my foods' page. if the my foods page is empty there will be a short description that reads "here you can add custom foods if you cannot find them in our database, eg 'my bread'...". below this will be a button titled 'add custom food'. if there are already food saved in the users 'my foods' page, these should be displayed here and the user can quick add them to the log for the date chosen on the carousel

this card will also include a quick 'actions button', on press the user will be given the option to 'copy yesterday' or 'copy from date'. these actions will copy the entirety of yesterdays lunch log, or present the user with a date selector and once confirmed, copy the entirety of the lunch log from the selected date

### card 3 - dinner

this card will have a button titled 'search food', on press a search bar will appear that allows the user to search for foods in a hosted copy of the open food facts database, as well as other data that will be added to the hosted db for generic foods. once an item has been selected the user will be presented with two  drop down menus to select the amount of the food being added to the log, one for a number (1,10,5,1000 etc) and one for the unit (g, kg, oz, floz, ml, litre, etc). once confirmed this data will be saved in the users log for that day at dinner.

this card will also include an 'add custom food' button, on press this will open the users custom food list, here they will be presented with a user created list from the 'my foods' page. if the my foods page is empty there will be a short description that reads "here you can add custom foods if you cannot find them in our database, eg 'my bread'...". below this will be a button titled 'add custom food'. if there are already food saved in the users 'my foods' page, these should be displayed here and the user can quick add them to the log for the date chosen on the carousel

this card will also include a quick 'actions button', on press the user will be given the option to 'copy yesterday' or 'copy from date'. these actions will copy the entirety of yesterdays dinner log, or present the user with a date selector and once confirmed, copy the entirety of the dinner log from the selected date

### card 4 - snacks

this card will have a button titled 'search food', on press a search bar will appear that allows the user to search for foods in a hosted copy of the open food facts database, as well as other data that will be added to the hosted db for generic foods. once an item has been selected the user will be presented with two  drop down menus to select the amount of the food being added to the log, one for a number (1,10,5,1000 etc) and one for the unit (g, kg, oz, floz, ml, litre, etc). once confirmed this data will be saved in the users log for that days snacks.

this card will also include an 'add custom food' button, on press this will open the users custom food list, here they will be presented with a user created list from the 'my foods' page. if the my foods page is empty there will be a short description that reads "here you can add custom foods if you cannot find them in our database, eg 'my bread'...". below this will be a button titled 'add custom food'. if there are already food saved in the users 'my foods' page, these should be displayed here and the user can quick add them to the log for the date chosen on the carousel

this card will also include a quick 'actions button', on press the user will be given the option to 'copy yesterday' or 'copy from date'. these actions will copy the entirety of yesterdays snack log, or present the user with a date selector and once confirmed, copy the entirety of the snack log from the selected date

# page 3 - my food

here the user can add custom food to be used in logging later.

on opening this page the user will be presented with any foods they have logged previously, if they dont yet have any logged foods there will be a short description that reads "here you can add custom foods if you cannot find them in our database, eg 'my bread'...". below this will be a button titled 'add custom food'. if there are already foods saved in the users 'my foods' page, these should be displayed here. there will be a button at the bottom of the list/page titled 'add food'. pressing this button will render a page with fields for the user to fill in. these fields should include; food name, amount (this field will be a empty field for a number), unit (a drop down for g, kg, ml, litre, oz, lbs, floz, etc), after this there will be a field for every macro nutrient listed in the macronutrient page, and every micronutrient listed in the micronutrient page. however the user will be able to fill out as much or as little of this information as they like before saving the food to the 'my foods' list.

the food name field should contain a default partially opaque prompt that reads "my bread..."

