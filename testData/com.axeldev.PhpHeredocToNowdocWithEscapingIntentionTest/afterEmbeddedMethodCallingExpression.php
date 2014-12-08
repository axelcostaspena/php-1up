<?php

$foo = <<<'EOT'
foo
EOT
    . $bar->baz(1.23, "qux$foo[bar]{$baz["qux"]}" . $foo) . <<<'EOT'
 bar
EOT;
