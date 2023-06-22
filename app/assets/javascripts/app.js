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

    // file upload name
    var fileName = document.querySelector('#upload-file-name');
    if( fileName ) {
        var radios = document.querySelectorAll('.govuk-radios__input');
        radios.forEach(function(radio) {
            radio.disabled = true;
        });
        var button = document.querySelector('#submit');
            button.disabled = true;
        var url = "/manage-pension-scheme-event-report/assets/javascripts/status-test.json";
        function pollName(){
            fetch(url).then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    return Promise.reject(response);
                }
            }).then(function (data) {
                console.log(data);
                if (!data.fileName) {
                    setTimeout(function() {
                        pollName();
                    }, 4000);
                } else {
                    fileName.innerHTML = data.fileName;
                    button.disabled = false;
                    button.classList.remove("govuk-button--disabled");
                    radios.forEach(function(radio) {
                        radio.disabled = false;
                    });
                }
            }).catch(function (err) {
                console.warn('Something went wrong.', err);
            });
        }
        pollName();
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
                console.log(data);
                if (data.status === "processing") {
                    setTimeout(function() {
                        pollData();
                    }, 4000);
                } else {
                    location.reload();
                }
            }).catch(function (err) {
                console.warn('Something went wrong.', err);
            });
        }
        pollData();
    }

});
