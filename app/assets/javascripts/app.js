// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
    window.history.replaceState(null, null, window.location.href);
}

document.addEventListener('DOMContentLoaded', function(event) {

     var backLink = document.querySelector('.govuk-back-link');
    if (backLink) {
        backLink.addEventListener('click', function(e) {
            e.preventDefault();
            if (window.history && window.history.back && typeof window.history.back === 'function') {
                window.history.back();
            }
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

    // file upload status
    var ajaxRedirect = document.querySelector('#processing-status');
    if( ajaxRedirect !== null ){
        var url = "/manage-pension-scheme-event-report/report/event-checking-file";
        function pollData(){
            fetch(url).then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    return Promise.reject(response);
                }
            }).then(function (data) {
                if (data.status === "processing") {
                    setTimeout(function() {
                        pollData();
                    }, 2000);
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
