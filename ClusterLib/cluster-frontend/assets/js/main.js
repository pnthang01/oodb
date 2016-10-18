/**
 * Global variables and functions
 */
var ProjectName = (function($, window) {
    var $btnMiniSidebar = $('.sidebar').find('.btn-toggle-sidebar'),
        $win = $(window),
        $html = $('html'),
        $body = $('body'),
        $myDropDown = $('[data-toggle="my-dropdown"]'),
        $niceScroll = $('.nice-scroll'),
        $table = $('.table'),
        // $initMinWidth = $('.init-minwidth'),
        $switchDate = $('.dropdown-date').find('.custom-switch'),
        $filterBarHead = $('.filter-bar-head'),
        $searchFilterBar = $('.filter-bar').find('.search-filter').find('input'),
        $listDragDrop = $('.list-drag-drop'),
        $selectInput = $('select.form-control'),
        $sourceTree = $('.source-tree'),
        $campaignBoard = $('.create-campaign-1').find('#campaign-board');

    function _toggleSidebar() {
        $html.toggleClass('sidebar-mini');

        setTimeout(function() {
            $win.trigger('resize.resizeHeaderTable');//trigger to resize header size on fixed-table.js
            reInitNiceScroll();
        }, 500);
    }

    function _setwidthItemAction(el) {
        var $itemAction = $(el).find('.item-action');

        if ($itemAction.length) {
            var $spanAction = $itemAction.find('span'),
                maxHeight = 0;

            maxHeight = Math.max.apply(null, $spanAction.map(function ()
            {
                return $(this).width();
            }).get());
            $spanAction.width(maxHeight);
        }
    }

    function initCustomCheckboxRadio() {
        var $checkboxRadio = $('.icheck');

        $checkboxRadio.iCheck({
            checkboxClass: 'icheckbox_square-blue',
            radioClass: 'iradio_square-blue',
            increaseArea: '5%'
        });
    }

    function _initNiceScroll() {
        if ($niceScroll.length) {
            $niceScroll.each(function(index, el) {
                var $el = $(el),
                    background,
                    cursor;

                if ($('.sidebar').has($el).length !== 0) {
                    background = '#242424';
                    cursor = '#666666';
                } else {
                    background = '#dedede';
                    cursor = '#aaa';
                }

                $el.niceScroll({
                    cursorcolor: cursor,
                    cursorborder: '0',
                    cursormaxheight: 30,
                    cursorborderradius: '99px',
                    autohidemode: false,
                    touchbehavior: true,
                    background: background,
                    cursorwidth: '8px',
                });
            });
        }
    }

    function reInitNiceScroll() {
        if ($niceScroll.length) {
            $niceScroll.each(function(index, el) {
                $(el).getNiceScroll().resize();
            });
        }
    }

    // function _initMinWidthOuterContent() {
    //     var maxWidthTable = 0,
    //         totalWidthItems = 0,
    //         minWidthOuterContent,
    //         paddingLeftMenu = 235;
    //     if ($table.length) {
    //         $table.each(function(index, el) {
    //             var $el = $(el);
    //             if ($el.width() > maxWidthTable) {
    //                 maxWidthTable = $el.width();
    //             }
    //         });
    //     }

    //     if ($initMinWidth.length) {
    //         var $itemChild = $initMinWidth.children();
    //         $itemChild.each(function(index, el) {
    //             totalWidthItems += $(el).outerWidth(true);
    //         });
    //     }

    //     if ($('html').hasClass('sidebar-mini')) {
    //         paddingLeftMenu = 50;
    //     }

    //     if (maxWidthTable > totalWidthItems) {
    //         minWidthOuterContent = maxWidthTable + paddingLeftMenu + 64; //64 is space outer and inner content
    //     } else {
    //         minWidthOuterContent = totalWidthItems + paddingLeftMenu + 64;
    //     }

    //     if ($win.width() < minWidthOuterContent) {
    //         $('.outer-content').css('minWidth', minWidthOuterContent);
    //     }
    // }

    function _showListDropdownCompare(e) {
        var $el = $(e.currentTarget),
            $checkbox = $el.find('input[type="checkbox"]');

        if ($checkbox.is(":checked")) {
            $el.closest('li').siblings('.collapse').addClass('in');
        } else {
            $el.closest('li').siblings('.collapse').removeClass('in');
        }
    }

    function _toggleMyDropdown(e) {
        var $el = $(e.currentTarget),
            $dropdownMenu = $el.closest('.dropdown').find('.dropdown-menu');

        $el.parent().toggleClass('open');
        _setwidthItemAction($dropdownMenu);
    }

    function _autoCloseMyDropDown(e) {
        if (!$('.dropdown').is(e.target) && $('.dropdown').has(e.target).length === 0 && $('.open').has(e.target).length === 0) {
            $('.dropdown').removeClass('open');
        }
    }

    function initStickyHeaderTable() {
        $table.each(function(index, el) {
            $(el).fixMe();
            $win.trigger('scroll.toogleHeaderTable');
        });
    }

    function _ShowDropDownSearch(e) {
        var $el = $(e.currentTarget);

        $el.closest('.dropdown').addClass('open');
    }

    function _initListDragDrop() {
        if ($listDragDrop.length) {
            $listDragDrop.each(function(index, el) {
                $(el).sortable({
                    items: '.drag'
                });
                $(el).disableSelection();
            });
        }
    }

    function _initWrapSelect() {
        if ($selectInput.length) {
            $selectInput.each(function(index, el) {
                $(el).wrap('<div class="select-custom"></div>');
            });
        }
    }

    function _initSourceTree() {
        if ($sourceTree.length) {
            $sourceTree.each(function(index, el) {
                var $el = $(el),
                    $text = $el.find('.has-child').children('.heading').children('strong');

                $text.on('click.openTree', function() {
                    $(this).closest('li').toggleClass('open');
                    $(this).closest('.nice-scroll').getNiceScroll().resize();
                });
            });
        }
    }

    function _stickycampaignBoard() {
        if ($campaignBoard.length) {
            var offsetTop = $campaignBoard.offset().top - $('.header').outerHeight();
            $campaignBoard.affix({
                offset: {
                    top: offsetTop
                }
            });
        }
    }

    function _bindEvent() {
        var timeout;
        $win.on('resize', function() {
            clearTimeout(timeout);
            timeout = setTimeout(function() {
                // _initMinWidthOuterContent();
            }, 200);
        });
        $myDropDown.on('click.toggleMyDropdown', _toggleMyDropdown);
        $btnMiniSidebar.on('click.toggleSidebar', _toggleSidebar);
        $switchDate.on('click.showListDropdownCompare', _showListDropdownCompare);
        $body.on('click.closeMyDropdown', _autoCloseMyDropDown);
        $win.on('scroll', function(){
            reInitNiceScroll();
        });
        $('.collapse').on('shown.bs.collapse', reInitNiceScroll);
        $('.modal').on('shown.bs.modal', reInitNiceScroll);
        $('.modal').on('hidden.bs.modal', reInitNiceScroll);
        $searchFilterBar.on('focus.enableDropDownSearch', _ShowDropDownSearch);
    }

    return {
        init : function() {
            _bindEvent();
            setTimeout(function() {
                initStickyHeaderTable();
            }, 500);
            initCustomCheckboxRadio();
            _initNiceScroll();
            // _initMinWidthOuterContent();
            _initListDragDrop();
            _initWrapSelect();
            _initSourceTree();
            // CreateCampaign1
            _stickycampaignBoard();
        },
        initCustomCheckboxRadio: initCustomCheckboxRadio,
        reInitNiceScroll: reInitNiceScroll
    };
}(jQuery, window));

jQuery(document).ready(function() {
    ProjectName.init();
});



