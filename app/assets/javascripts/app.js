// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
    window.history.replaceState(null, null, window.location.href);
}

document.addEventListener('DOMContentLoaded', function(event) {

    // handle back click
    var backLink = document.querySelector('.govuk-back-link');
    if (backLink !== null) {
        backLink.addEventListener('click', function(e){
            e.preventDefault();
            e.stopPropagation();
            window.history.back();
        });
    }

    // handle country picker
    var selectEl = document.querySelector('#country')
    if( selectEl !== null ){
        accessibleAutocomplete.enhanceSelectElement({
            defaultValue: "",
            selectElement: selectEl
        })
        // fix to ensure error when blank
        document.querySelector('input[role="combobox"]').addEventListener('keydown', function(e){
            if (e.which != 13 && e.which != 9) {
                selectEl.value = "";
            }
        });
    }

    // handle paymentDate focus
    var paymentDate = document.querySelector('#paymentDate');
    var links = document.querySelectorAll('.govuk-error-summary__list li a');
    if (paymentDate !== null && links !== null) {
        links.forEach(amendLink);
        function amendLink(link){
            if(link.href.includes("#paymentDate")){
                link.href = link.href + ".day";
            }
        }
    }

});
