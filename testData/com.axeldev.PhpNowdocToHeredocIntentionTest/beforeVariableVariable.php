<?php

$vv_key_key = "vv_key";
$vv_key = "vv";
$vv = "variable variable";
echo <<<'EOT'
foo<caret> {$$$vv_key_key} bar
EOT;
