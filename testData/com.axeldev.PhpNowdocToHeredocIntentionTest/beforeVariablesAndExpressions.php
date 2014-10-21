<?php

$foo = <<<'EOT'
foo<caret> $bar $baz[qux] {$foo->bar($baz, 1)}
EOT;
