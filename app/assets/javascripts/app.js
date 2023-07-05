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

    // handle print link
    var printLink = document.querySelector('#print-this-page-link');
    if (printLink) {
        printLink.addEventListener('click', function (e) {
            window.print();
            return false;
        });
    }

    // handle country picker
    var selectEl = document.querySelector('#country')
    if( selectEl !== null ){
        accessibleAutocomplete.enhanceSelectElement({
            defaultValue: "",
            selectElement: selectEl
        })
        document.querySelector('input[role="combobox"]').addEventListener('keydown', function(e){
            if (e.which != 13 && e.which != 9) {
                selectEl.value = "";
            }
        });
    }

    // file upload status
    var ajaxRedirect = document.querySelector('#processing-status');
    if( ajaxRedirect ) {
        var url = "/manage-pension-scheme-event-report/new-report/event-checking-file";
        function pollData(){
            fetch(url).then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    return Promise.reject(response);
                }
            }).then(function (data) {
                if (data.status === "processing") {
                    console.log(data);
                    setTimeout(function() {
                        pollData();
                    }, 4000);
                } else {
                    console.log(data);
                    location.reload();
                }
            }).catch(function (err) {
                console.warn('Something went wrong.', err);
            });
        }
        pollData();
    }

});
