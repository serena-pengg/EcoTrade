'use strict';

$(document).ready(function () {

    // set logger position
    alertify.logPosition("bottom right");

    $('.toggle-dash-menu').on('click', function () {
        if(window.innerWidth < 760){
            $('.dash-side-nav-wrapper').toggleClass('slide-in');
        }else{
            $('.dash-side-nav-wrapper').toggleClass('slide-out');
            $('.top-nav').toggleClass('full-width');
            $('.content-wrapper').toggleClass('full-width');
        }
    });

    //hide side nav
    var sideMenu = document.getElementById('dash-side-menu'),
        replyBox = document.getElementById('ux-box'),
        siteMenu = document.getElementById('side-nav'),
        searchBox = document.getElementById('dash-search-box');
    window.onclick = function(event) {
        if (event.target === sideMenu) {
            $('.dash-side-nav-wrapper').toggleClass('slide-in');
        }else if (event.target === searchBox) {
            $('.dash-search-box').removeClass('show-search');
        }else if(event.target === siteMenu){
            $('.side-nav').removeClass('show-side-menu');
            $('body').removeClass('no-scroll');
        }else if(event.target === replyBox){
            $('.ux-box-wrapper').removeClass('show-ux-box');
        }
    };

    $('.show-search').on('click', function (e) {
       $('.dash-search-box').addClass('show-search');
        e.preventDefault();
    });
    $('.nav-toggle').on('click', function () {
        $('body').addClass('no-scroll');
        $('.side-nav').addClass('show-side-menu');
    });

    // delete review
    $('.confirm-action').on('click', function (e) {
        var _myThis = $(this);
        // confirm dialog
        alertify.confirm("Are you Sure?", function () {
            // user clicked "ok"
            // some logic here to talk to the server
            _myThis.closest('.review-item').fadeOut();
            alertify.success("Review Deleted Successfully");
        }, function() {
            // user clicked "cancel"
            alertify.error("You've clicked Cancel")
        });
        e.preventDefault();
    });

    // delete dashboard item
    $('.delete-item').on('click', function (e) {
        var _myThis = $(this);
        // confirm dialog
        alertify.confirm("Are you Sure?", function () {
            // user clicked "ok"
            // some logic here to talk to the server
            _myThis.closest('.product-item').fadeOut();
            alertify.success("Item Deleted Successfully");
        }, function() {
            // user clicked "cancel"
            alertify.error("You've clicked Cancel")
        });
        e.preventDefault();
    });

    $('.show-box').on('click', function (e) {
       $('.ux-box-wrapper').addClass('show-ux-box');
        e.preventDefault();
    });
    $('.close-box').on('click', function (e) {
        $('.ux-box-wrapper').removeClass('show-ux-box');
        e.preventDefault();
    });


    //chat box js
    var chatsContainer = document.querySelector('.chat-flow');
    var $chatsContainer = $('.chat-flow');
    $('.chat-box-btn').on('click', function (e) {
       $('.chat-box').toggleClass('show-chat');
       $('.chat-top section').toggleClass('slide-s');
       $('.icon-sw').toggleClass('fa-comment fa-times');
        e.preventDefault();
    });

    // 语音朗读功能
    let speechSynthesis = window.speechSynthesis;
    let voices = [];

    // 全局变量
    let currentUtterance = null;
    let currentButton = null;
    let isPaused = false;
    let isEnded = true;

    // 初始化语音列表
    function initVoices() {
        voices = speechSynthesis.getVoices();
        console.log('Available voices:', voices);

        // 如果没有找到中文语音，尝试使用默认语音
        if (!voices.some(voice => voice.lang.includes('zh'))) {
            console.log('No Chinese voice found, using default voice');
        }
    }

    // 等待语音列表加载
    if (speechSynthesis) {
        if (speechSynthesis.onvoiceschanged !== undefined) {
            speechSynthesis.onvoiceschanged = initVoices;
        }
        initVoices();
    }

    function getChineseVoice() {
        const voices = speechSynthesis.getVoices();
        let zhVoice = voices.find(v => v.lang === 'zh-CN');
        if (!zhVoice) {
            zhVoice = voices.find(v => v.lang && v.lang.indexOf('zh') !== -1);
        }
        return zhVoice || voices[0] || null;
    }

    function addMessage(text, type) {
        const messageWrapper = document.createElement('div');
        messageWrapper.className = type + '-wrapper';

        const messageContent = document.createElement('div');
        messageContent.className = 'message-content';
        messageContent.textContent = text;

        if (type === 'inbound') {
            messageWrapper.appendChild(messageContent);
            const speakButton = document.createElement('button');
            speakButton.className = 'speak-button';
            speakButton.innerHTML = '<i class="fa fa-volume-up"></i>';
            speakButton.title = '播放语音';
            speakButton.dataset.state = 'idle'; // idle, playing, paused, ended

            speakButton.onclick = function() {
                if (speakButton.dataset.state === 'idle' || speakButton.dataset.state === 'ended') {
                    playSpeech(text, speakButton);
                } else if (speakButton.dataset.state === 'playing') {
                    pauseSpeech(speakButton);
                } else if (speakButton.dataset.state === 'paused') {
                    resumeSpeech(speakButton);
                }
            };

            speakButton.setState = function(state) {
                speakButton.dataset.state = state;
                if (state === 'playing') {
                    speakButton.classList.add('speaking');
                    speakButton.innerHTML = '<span class="wave"></span><i class="fa fa-pause"></i>';
                } else if (state === 'paused') {
                    speakButton.classList.remove('speaking');
                    speakButton.innerHTML = '<i class="fa fa-play"></i>';
                } else if (state === 'ended') {
                    speakButton.classList.remove('speaking');
                    speakButton.innerHTML = '<i class="fa fa-redo"></i>';
                } else {
                    speakButton.classList.remove('speaking');
                    speakButton.innerHTML = '<i class="fa fa-volume-up"></i>';
                }
            };

            messageWrapper.appendChild(speakButton);
        } else {
            messageWrapper.appendChild(messageContent);
        }

        chatsContainer.appendChild(messageWrapper);
        chatsContainer.scrollTop = chatsContainer.scrollHeight;
        $chatsContainer.perfectScrollbar('update');
    }

    function playSpeech(text, button) {
        if (!speechSynthesis) return;
        if (speechSynthesis.speaking || speechSynthesis.pending) {
            speechSynthesis.cancel();
        }
        if (currentButton && currentButton !== button) {
            currentButton.setState('idle');
        }
        currentButton = button;
        isPaused = false;
        isEnded = false;

        // 文本过长分段
        const maxLen = 100;
        const segments = [];
        for (let i = 0; i < text.length; i += maxLen) {
            segments.push(text.slice(i, i + maxLen));
        }

        let segIndex = 0;
        function speakNextSegment() {
            if (segIndex >= segments.length) {
                button.setState('ended');
                isEnded = true;
                currentUtterance = null;
                return;
            }
            const utterance = new SpeechSynthesisUtterance(segments[segIndex]);
            currentUtterance = utterance;
            utterance.lang = 'zh-CN';
            utterance.rate = 1.0;
            utterance.pitch = 1.0;
            utterance.volume = 1.0;
            const chineseVoice = getChineseVoice();
            if (chineseVoice) {
                utterance.voice = chineseVoice;
            } else {
                alert('您的浏览器未安装中文语音包，语音播放可能无法正常工作。');
            }
            utterance.onstart = function() {
                button.setState('playing');
            };
            utterance.onend = function() {
                segIndex++;
                speakNextSegment();
            };
            utterance.onerror = function(e) {
                button.setState('idle');
                currentUtterance = null;
                alert('语音播放失败，可能是浏览器未安装中文语音包或文本过长。');
            };
            speechSynthesis.speak(utterance);
        }
        speakNextSegment();
    }

    function pauseSpeech(button) {
        if (speechSynthesis.speaking && !speechSynthesis.paused) {
            speechSynthesis.pause();
            button.setState('paused');
            isPaused = true;
        }
    }

    function resumeSpeech(button) {
        if (speechSynthesis.paused) {
            speechSynthesis.resume();
            button.setState('playing');
            isPaused = false;
        }
    }

    document.addEventListener('visibilitychange', function() {
        if (document.hidden && speechSynthesis.speaking) {
            speechSynthesis.cancel();
            if (currentButton) currentButton.setState('idle');
        }
    });

    $('.chat-form').on('submit', function (e) {
        var chatInput = $(this).find('.chat-message');
        if(chatInput.val() !== ''){
            // 添加用户消息
            addMessage(chatInput.val(), 'outbound');
            
            // 发送消息到服务器
            $.ajax({
                url: '/api/chat/send',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ message: chatInput.val() }),
                success: function(response) {
                    // 添加AI回复
                    addMessage(response, 'inbound');
                },
                error: function(xhr, status, error) {
                    console.error('Error:', error);
                    console.error('Status:', status);
                    console.error('Response:', xhr.responseText);
                    addMessage('抱歉，发生了错误，请稍后重试。', 'inbound');
                }
            });
            
            chatInput.val('');
        }
        e.preventDefault();
    });
    $chatsContainer.perfectScrollbar();

    //side nav menu
    $('.perfect-scroll').perfectScrollbar();
    $(window).on('resize', function () {
        $('.perfect-scroll').perfectScrollbar('update');
    });


    //----------cart js
    //remove item from cart
    $('.remove-item').on('click', function () {
        $(this).closest('tr').fadeOut().remove();
        $('.sub-total').text(total());
    });

    //increase/decrease item quantity buttons
    $('.add-item').on('click', function () {
        var input = $(this).parent().find('.quantity-input');
        input.val( function(i, oldval) {
            return ++oldval;
        }).change();
        $('.sub-total').text(total());
    });
    $('.minus-item').on('click', function () {
        var input = $(this).parent().find('.quantity-input');
        if(input.val() > 0){
            input.val( function(i, oldval) {
                return --oldval;
            }).change();
        }
        $('.sub-total').text(total());
    });

    //calculate totals
    $('.quantity-input').on('change', function () {
        var unitPrice = $(this).closest('.price-control').find('.unit-price span').data('price');
        $(this).closest('tr').find('.total').text(parseFloat($(this).val()) * parseFloat(unitPrice))
    });

    function total() {
        var totalPrice = 0;

        $.each($('.total'), function (index, value) {
            totalPrice += parseFloat(value.textContent);
        });
        return totalPrice
    }

    //switch auth forms
    var switchLink = $('.switch-nav a');
    switchLink.on('click', function (e) {
        var _this = $(this);
        switchLink.removeClass('current');
        _this.addClass('current');
        $('.forms-c form').hide();
        $('.'+_this.data('form')).show().addClass('fadeIn');
        e.preventDefault();
    });

    // show password
    $('.reveal-password').on('click', function () {
        $(this).toggleClass('fa-eye fa-eye-slash');
        var passInput = $(this).parent().find('.form-control');
        if($(this).hasClass('fa-eye-slash')){
            passInput.attr('type', 'text')
        }else{
            passInput.attr('type', 'password')
        }
    });

    //details page show more
    $('.show-more').on('click', function (e) {
        $('.description').toggleClass('max-height');
        $(this).find('i').toggleClass('fa-angle-double-down fa-angle-double-up');
        e.preventDefault();
    });

    //user image select
    window.URL = window.URL || window.webkitURL;
    var fileInput = document.getElementById('file'),
        imgElement = document.getElementById('user-photo');
    $('.img-file').on('change', function () {
        var imgFile = fileInput.files[0];
        if (imgFile.type.indexOf('image/') == 0){
            imgElement.src = window.URL.createObjectURL(imgFile);
            alertify.success("Photo Selected Successfully");
            imgElement.onload = function() {
                // release object url
                window.URL.revokeObjectURL(this.src);
            }
        }else{
            alertify.error("File not Image!");
        }
    });


    // jquery knob
    $(".dial").knob({
        draw : function () {

            // "tron" case
            if(this.$.data('skin') == 'tron') {

                this.cursorExt = 0.3;

                var a = this.arc(this.cv)  // Arc
                    , pa                   // Previous arc
                    , r = 1;

                this.g.lineWidth = this.lineWidth;

                if (this.o.displayPrevious) {
                    pa = this.arc(this.v);
                    this.g.beginPath();
                    this.g.strokeStyle = this.pColor;
                    this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, pa.s, pa.e, pa.d);
                    this.g.stroke();
                }

                this.g.beginPath();
                this.g.strokeStyle = r ? this.o.fgColor : this.fgColor ;
                this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, a.s, a.e, a.d);
                this.g.stroke();

                this.g.lineWidth = 2;
                this.g.beginPath();
                this.g.strokeStyle = this.o.fgColor;
                this.g.arc( this.xy, this.xy, this.radius - this.lineWidth + 1 + this.lineWidth * 2 / 3, 0, 2 * Math.PI, false);
                this.g.stroke();

                return false;
            }
        }
    });

    //carousel slider initialization
    $('.owl-carousel').owlCarousel({
        animateOut: 'slideOutLeft',
        animateIn: 'slideInRight',
        items:1,
        margin:30,
        stagePadding:30,
        smartSpeed:450,
        autoplay: true,
        loop: true
    });

    //summernote
    $('#summernote').summernote({
        placeholder: 'Item Description Goes Here...',
        tabsize: 2,
        height: 200
    });

    //Dropzone
    $("#ur-dropzone").dropzone({
        url: "#" // url to upload files to
    });

    //typed js initialization

    var element = $(".title-style2 span");
    element.typed({
        strings: ["Get what you want", "Sell Your old items.", "Find what you need.", "Join Millions of Sellers & Buyers."],
        typeSpeed: 100,
        loop: true,
        smartBackspace: true
    });

    // 聊天窗口大小调整功能
    function initChatBoxResize() {
        const chatBox = document.querySelector('.chat-box');
        if (!chatBox) return;

        // 先移除旧的mousedown事件，防止重复绑定
        chatBox.onmousedown = null;

        let isResizing = false;
        let startX, startY, startWidth, startHeight;

        chatBox.addEventListener('mousedown', function(e) {
            // 检查是否点击了调整大小的区域（左上角三角形）
            const rect = chatBox.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            if (x <= 20 && y <= 20) {
                isResizing = true;
                startX = e.clientX;
                startY = e.clientY;
                startWidth = chatBox.offsetWidth;
                startHeight = chatBox.offsetHeight;

                // 添加事件监听器
                document.addEventListener('mousemove', handleMouseMove);
                document.addEventListener('mouseup', handleMouseUp);

                // 防止文本选择
                e.preventDefault();
            }
        });

        function handleMouseMove(e) {
            if (!isResizing) return;

            // 计算鼠标移动距离
            const deltaX = startX - e.clientX;
            const deltaY = startY - e.clientY;

            // 计算新的宽度和高度，增加灵敏度
            let newWidth = startWidth + deltaX;
            let newHeight = startHeight + deltaY;

            // 确保不小于最小尺寸
            newWidth = Math.max(300, newWidth);
            newHeight = Math.max(450, newHeight);

            // 确保不大于最大尺寸
            newWidth = Math.min(800, newWidth);
            newHeight = Math.min(800, newHeight);

            // 应用新的尺寸，使用transform来优化性能
            chatBox.style.width = newWidth + 'px';
            chatBox.style.height = newHeight + 'px';

            // 更新滚动条
            const chatsContainer = chatBox.querySelector('.chat-flow');
            if (chatsContainer && chatsContainer.perfectScrollbar) {
                chatsContainer.perfectScrollbar('update');
            }
        }

        function handleMouseUp() {
            isResizing = false;
            document.removeEventListener('mousemove', handleMouseMove);
            document.removeEventListener('mouseup', handleMouseUp);
        }
    }

    // 在页面加载完成后初始化
    document.addEventListener('DOMContentLoaded', initChatBoxResize);

    // 在聊天窗口显示时也初始化
    document.querySelector('.chat-box-btn').addEventListener('click', function() {
        setTimeout(initChatBoxResize, 100); // 给一点时间让聊天窗口显示
    });

    // 语音识别功能
    let recognition = null;
    let isRecording = false;

    // 初始化语音识别
    function initSpeechRecognition() {
        console.log('Initializing speech recognition...');

        // 检查浏览器是否支持语音识别
        if (!('webkitSpeechRecognition' in window)) {
            console.warn('您的浏览器不支持语音识别功能');
            const voiceButton = document.getElementById('voiceInputButton');
            if (voiceButton) {
                voiceButton.style.display = 'none';
            }
            return;
        }

        try {
            recognition = new webkitSpeechRecognition();
            console.log('Speech recognition object created successfully');

            recognition.continuous = false;
            recognition.interimResults = false;
            recognition.lang = 'zh-CN';

            recognition.onstart = function() {
                console.log('Speech recognition started');
                isRecording = true;
                const voiceButton = document.getElementById('voiceInputButton');
                if (voiceButton) {
                    voiceButton.classList.add('recording');
                    voiceButton.innerHTML = '<i class="fa fa-stop"></i>';
                    voiceButton.title = '停止录音';
                }
            };

            recognition.onend = function() {
                console.log('Speech recognition ended');
                isRecording = false;
                const voiceButton = document.getElementById('voiceInputButton');
                if (voiceButton) {
                    voiceButton.classList.remove('recording');
                    voiceButton.innerHTML = '<i class="fa fa-microphone"></i>';
                    voiceButton.title = '语音输入';
                }
            };

            recognition.onresult = function(event) {
                console.log('Speech recognition result received');
                const transcript = event.results[0][0].transcript;
                const messageInput = document.getElementById('messageInput');
                if (messageInput) {
                    messageInput.value = transcript;
                }
            };

            recognition.onerror = function(event) {
                console.error('语音识别错误:', event.error);
                alertify.error('语音识别失败，请重试');
                isRecording = false;
                const voiceButton = document.getElementById('voiceInputButton');
                if (voiceButton) {
                    voiceButton.classList.remove('recording');
                    voiceButton.innerHTML = '<i class="fa fa-microphone"></i>';
                    voiceButton.title = '语音输入';
                }
            };

            // 绑定语音输入按钮点击事件
            const voiceButton = document.getElementById('voiceInputButton');
            if (voiceButton) {
                voiceButton.onclick = function(e) {
                    e.preventDefault();
                    console.log('Voice button clicked, isRecording:', isRecording);

                    if (!recognition) {
                        console.error('Speech recognition not initialized');
                        return;
                    }

                    if (isRecording) {
                        console.log('Stopping recognition...');
                        recognition.stop();
                    } else {
                        try {
                            console.log('Starting recognition...');
                            recognition.start();
                        } catch (error) {
                            console.error('启动语音识别失败:', error);
                            alertify.error('启动语音识别失败，请重试');
                        }
                    }
                };
            }
        } catch (error) {
            console.error('初始化语音识别失败:', error);
            alertify.error('初始化语音识别失败，请刷新页面重试');
        }
    }

    // 在页面加载完成后初始化语音识别
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSpeechRecognition);
    } else {
        initSpeechRecognition();
    }

});

// page loader

$(window).on('load', function(){
    $('.page-loader').fadeOut('slow',function(){
        $(this).remove();
    });
});
