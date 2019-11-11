'use strict';

function test() {
   const mapToggler = document.getElementById('map-toggler');
   const vlsFilter = document.querySelector(".filter.filter-vls");
   const alsFilter = document.querySelector(".filter.filter-als");
   console.log(vlsFilter, alsFilter);
   if(mapToggler) {
      mapToggler.style.display = "none";
      mapToggler.click();
      relocate();
      if(vlsFilter)
         vlsFilter.click();
      if(alsFilter)
         alsFilter.click();
   }
}

// more Javascript