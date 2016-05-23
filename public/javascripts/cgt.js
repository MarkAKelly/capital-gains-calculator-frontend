$(document).ready($(function() {

    $('*[data-hidden]').each(function() {

        var $self = $(this);
        var $hidden = $('#hidden')
        var $input = $self.find('input');

        if ($input.val() === 'Yes' && $input.prop('checked')) {
            $hidden.show();
        } else {
            $hidden.hide();
        }

        $input.change(function() {

            var $this = $(this);

            if ($this.val() === 'Yes') {
                $hidden.show();
            } else if($this.val() === 'No') {
                $hidden.hide();
            }
        });
    });

    $(function() {
        $('input[type="radio"]').each(function() {
            var o = $(this).parent().next('.additional-option-block');
            if ($(this).prop('checked')) {
                o.show();
            } else {
                o.hide();
            }
        });

        $('input[type="radio"]').on('click', function(e){
            var o = $(this).parent().next('.additional-option-block');
            if(o.index() == 1){
                $('.additional-option-block').hide();
                o.show();
            }
        });
    });
}));

