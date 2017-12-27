$(document).ready(function(){
    var floor=0;
    var limit=0;
    setInterval(function(){
        if(floor<limit){
            floor+=1;
        }else{
            floor=0;
        }
    },10000);

//    var wsServer = 'ws://192.168.1.119:9502';
    var wsServer = 'ws://101.201.28.83:86';
    var websocket = new WebSocket(wsServer);
    //onopen监听连接打开
    websocket.onopen = function (evt) {
        websocket.send(JSON.stringify({sushe:'1_1'}));
    };

    
    //监听连接关闭
    //    websocket.onclose = function (evt) {
    //        console.log("Disconnected");
    //    };

    //onmessage 监听服务器数据推送
    websocket.onmessage = function (evt) {
       var data=JSON.parse(evt.data).data;
       console.log(data);
       if(data){
           $('.the-floor ul').empty();
           $('.circle-list').empty();
           limit=data.list.floor.length-1;
            $.each(data.list.floor,function(i,v){
                var floor_li='\
                        <li>\
                            <h2>'+v.in+'/'+v.total+'</h2>\
                            <p><span style="height:'+v.in/v.total*100+'%"></span></p>\
                        </li>'
                $('.the-floor ul').append(floor_li);
                var bedroom_li='\
                    <li class="circle-list-li" mess_id="'+v.total+'">\
                        <ul></ul>\
                    </li>'
                $('.circle-list').append(bedroom_li);
                $.each(v.bedroom,function(inx,val){
                    var dormroom_li='\
                        <li mess_id="'+val.total+'">\
                            <div></div>\
                            <h4>'+val.in+'/'+val.total+'</h4>\
                            <p>'+val.name+'</p>\
                        </li>'
                    $('.circle-list-li').eq(i).find('ul').append(dormroom_li);
                    var myChart = echarts.init($('.circle-list-li').eq(i).find('li:last-child').find('div')[0]);
                    var option = {
                        series: [
                            {
                                name:'',
                                type:'pie',
                                radius: ['70%', '80%'],
                                avoidLabelOverlap: true,
                                hoverAnimation:false,
                                clockwise:false,
                                label: {
                                    normal: {
                                        show: false,
                                        position: 'center'
                                    },
                                    emphasis: {
                                        show: true,
                                        textStyle: {
                                            fontSize: '8',
                                            fontWeight: 'bold'
                                        }
                                    }
                                },
                                labelLine: {
                                    normal: {
                                        show: false
                                    }
                                },
                                data:[
                                    {value:val.in, name:'在寝'+val.in},
                                    {value:val.total, name:'总数'+val.total},
                                ]
                            }
                        ],
                        color:['#9EE6AB','#202227']
                    };
                    myChart.setOption(option);
                })
            })
            $('.the-floor ul li h2').hide();
            $('.the-floor ul').find('li').eq(floor).addClass('on').find('h2').show();
            $('.the-floor h1').text(floor+1+'楼情况');
            $('.circle-list').css({'margin-top':-floor*240+'px','transition':'2s'});

       }
       
    };
    setTimeout(function(){
        location.reload();
    },300000)
})