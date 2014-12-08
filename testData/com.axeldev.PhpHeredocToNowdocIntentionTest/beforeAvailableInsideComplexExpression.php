<?php

$foo = <<<EOT
bar {$baz->baz($qux["foo"],<caret> 123, "bar $baz[qux]")}
EOT;
