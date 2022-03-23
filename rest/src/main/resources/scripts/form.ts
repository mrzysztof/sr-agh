const rangeForm = document.getElementById("rangeForm") as HTMLFormElement;
const yearSelect = document.getElementById("yearSelect") as HTMLSelectElement;
const regionSelect = document.getElementById("regionSelect") as HTMLSelectElement;

rangeForm.oninput = () => {
    if(yearSelect.selectedIndex > 0){
        rangeForm.submit();
    }
}