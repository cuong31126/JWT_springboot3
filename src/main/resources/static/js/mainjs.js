$(document).ready(function() {
    // Hiển thị thông tin người dùng đăng nhập thành công
    if (window.location.pathname.includes("/user/profile") || $("#profile").length > 0) {
        if (localStorage.token) {
            $.ajax({
                type: 'GET',
                url: '/users/me',
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                beforeSend: function(xhr) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.token);
                },
                success: function(data) {
                    $('#profile').html(data.fullName);
                    if ($('#emailInfo').length > 0) {
                        $('#emailInfo').html('Email: ' + data.email);
                    }
                    if (data.images && document.getElementById("images")) {
                        document.getElementById("images").src = data.images;
                    }
                },
                error: function(e) {
                    alert("Sorry, you are not logged in or token expired.");
                    localStorage.removeItem('token');
                    window.location.href = "/login";
                }
            });
        } else {
            alert("Sorry, you are not logged in.");
            window.location.href = "/login";
        }
    }

    // Hàm Login
    $('#login').click(function() {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;

        if (!email || !password) {
            alert("Vui lòng nhập email và mật khẩu!");
            return;
        }

        var basicInfo = JSON.stringify({
            email: email,
            password: password
        });

        $.ajax({
            type: "POST",
            url: "/auth/login",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: basicInfo,
            success: function(data) {
                localStorage.token = data.token;
                window.location.href = "/user/profile";
            },
            error: function(xhr) {
                alert("Login Failed: " + (xhr.responseJSON ? xhr.responseJSON.description || xhr.responseJSON.detail : "Sai thông tin tài khoản"));
            }
        });
    });

    // Hàm đăng xuất
    $('#logout').click(function() {
        localStorage.clear();
        window.location.href = "/login";
    });
});
