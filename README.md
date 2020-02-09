# ShopManager

## instalation
  This mod requires fabrc to be installed more in that here:https://fabricmc.net/, After that just drop the jar from the releases page in your mods folder
  
## Usage

   ### Recording a shop
   To record the prices at a shop first you need to warp the shop then run `/sm startrec <shopname>` shop name should be the name of   
   the shop or warp to goto the shop to save what you have recorded you must run `/sm stoprec`. When recording a shop to try and    
   prevend recording unreachable signs it onle record rendered signs so turn around to make shure all signs in a shop are recorded.
   
   ### Updating a shop
   If prices change or a item has ben added.removed you will need to update the recording of that shop to do that just run 
   `/sm startrec <shopname>`,`/sm stoprec` with the same shopname the previus recording will be DELETED and overriten
   
   ### Deleating a shop
   Go to a place with no valid shop signs and run `/sm startrec <shopname>`,`/sm stoprec` again
   
   ### Searching for an item
   To find buy and sell prices for an item you un `/sm <buy|sell> {itemid|hand} {quantity}` hand will use the item you are holding in        your main hand,item id needs the internal minecraft id for exampls `iron_ingot,oak_log`
   
   
(total cost) (price per item) (listed quantity) (shopname)
![Alt text](/Capture.PNG?raw=true )

## Supported sign types 
  This was made for a plugin called chestshop where the formattion for a sign is:\
    1. username of the player\
    2. quantity being sold\
    3. s3:b5 (s=sell,b=buy)\
    4. item name truncated to fit on a sign\
  it may not work correctly for other plugins if you make a issue and provide detail about the formatting and pictures, i will try and 
  add more.

  When recording a shop the texture of a sign is changed if the sign is detecded as a valid sign it will have the texture of oak,
  invalid will use the dark oak texture

## Q/A
  ### Will this work for multiple servers?
  Yes, When you record a shop it save what the server ip is and when you search for prices it will only show shops on that servers ip
  
  
  
  
